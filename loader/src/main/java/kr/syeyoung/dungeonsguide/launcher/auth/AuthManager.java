package kr.syeyoung.dungeonsguide.launcher.auth;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.launcher.auth.token.*;
import kr.syeyoung.dungeonsguide.launcher.events.AuthChangedEvent;
import kr.syeyoung.dungeonsguide.launcher.exceptions.AuthFailedExeption;
import kr.syeyoung.dungeonsguide.launcher.exceptions.PrivacyPolicyRequiredException;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.concurrent.*;


public class AuthManager {
    Logger logger = LogManager.getLogger("AuthManger");

    private static AuthManager INSTANCE;

    public static AuthManager getInstance() {
        if(INSTANCE == null) INSTANCE = new AuthManager();
        return INSTANCE;
    }

    @Setter
    private String baseserverurl = "https://dungeons.guide";

    private AuthToken currentToken = new NullToken();

    public AuthToken getToken() {
        return currentToken;
    }
    public String getWorkingTokenOrNull() {
        if (currentToken instanceof DGAuthToken) return currentToken.getToken();
        else return null;
    }


    private volatile boolean initlock = false;

    public void init() {
        if (initlock) {
            logger.info("Cannot init AuthManger twice");
            return;
        }


        initlock = true;

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("DgAuth Pool").build();
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, namedThreadFactory);
        scheduler.scheduleAtFixedRate(() -> {
            boolean shouldReAuth = false;
            if (!getToken().getUID().replace("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID())) {
                shouldReAuth = true;
            }
            if (!getToken().isAuthenticated()) {
                shouldReAuth = true;
            }
            if (shouldReAuth)
                reAuth();
        }, 10,2000, TimeUnit.MILLISECONDS);


        reAuth();
    }


    private volatile boolean reauthLock = false;


    AuthToken reAuth() {
        if (reauthLock) {
            while(reauthLock);
            return currentToken;
        }

        reauthLock = true;

        try {
            String token = DgAuthUtil.requestAuth(baseserverurl);
            byte[] encSecret = DgAuthUtil.checkSessionAuthenticityAndReturnEncryptedSecret(token);
            currentToken = DgAuthUtil.verifyAuth(token, encSecret, baseserverurl);
            MinecraftForge.EVENT_BUS.post(new AuthChangedEvent(currentToken));

            if (currentToken instanceof PrivacyPolicyRequiredToken) throw new PrivacyPolicyRequiredException();
        } catch (NoSuchAlgorithmException | AuthenticationException | IOException | NoSuchPaddingException |
                 InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException e) {
            currentToken = new FailedAuthToken(e);
            // TODO: loader notifications on bottom right?
//            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §r§cDG auth failed, trying again in ten seconds", true);
            logger.error("Re-auth failed with message {}, trying again in a 2 seconds", String.valueOf(Throwables.getRootCause(e)));
            throw new AuthFailedExeption(e);
        } finally {
            reauthLock = false;
        }
        return currentToken;
    }


    AuthToken acceptPrivacyPolicy() throws IOException {
        if (reauthLock) {
            while(reauthLock);
            return currentToken;
        }

        if (currentToken instanceof PrivacyPolicyRequiredToken) {
            reauthLock = true;
            try {
                currentToken = DgAuthUtil.acceptNewPrivacyPolicy(currentToken.getToken(), baseserverurl);
                if (currentToken instanceof PrivacyPolicyRequiredToken) throw new PrivacyPolicyRequiredException();
            } catch (IOException e) {
                currentToken = new FailedAuthToken(e);
                // TODO: loader notifications on bottom right?
                logger.error("Accepting Privacy Policy failed with message {}, trying again in a 2 seconds", String.valueOf(Throwables.getRootCause(e)));
                throw e;
            } finally {
                reauthLock = false;
            }
        }
        return currentToken;
    }
}
