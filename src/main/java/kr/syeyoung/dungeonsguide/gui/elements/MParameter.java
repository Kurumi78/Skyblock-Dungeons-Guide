package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.roomedit.panes.DynamicEditor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class MParameter extends MPanel {
    private MLabel label;
    private MLabel data;

    @Getter @Setter
    private Color hover = Color.gray;

    @Getter @Setter
    private Parameter parameter;
    private DynamicEditor processorParameterEditPane;

    public MParameter(final Parameter parameter, DynamicEditor processorParameterEditPane) {
        this.processorParameterEditPane = processorParameterEditPane;
        this.add(this.label = new MLabel() {
            @Override
            public String getText() {
                return parameter.getName();
            }
        });
        this.add(this.data = new MLabel() {
            @Override
            public String getText() {
                return parameter.getNewData() != null ?parameter.getNewData().toString() :"-empty-";
            }
        });
        this.label.setAlignment(MLabel.Alignment.LEFT);
        this.data.setAlignment(MLabel.Alignment.RIGHT);

        this.parameter = parameter;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (hover != null && new Rectangle(new Point(0,0),getBounds().getSize()).contains(relMousex0, relMousey0)) {
            Gui.drawRect(0,0,getBounds().width, getBounds().height, hover.getRGB());
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (this.getBounds().x > -20 && lastAbsClip.contains(absMouseX, absMouseY)) {
            // open new gui;
            EditingContext.getEditingContext().openGui(new GuiDungeonParameterEdit(this, processorParameterEditPane));
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, getBounds().height));
        label.setBounds(new Rectangle(0,0,parentHeight / 3, getBounds().height));
        data.setBounds(new Rectangle(parentWidth / 3,0,parentWidth / 3 * 2, getBounds().height));
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,getBounds().width / 3, getBounds().height));
        data.setBounds(new Rectangle(getBounds().width / 3,0,getBounds().width / 3 * 2, getBounds().height));
    }
}