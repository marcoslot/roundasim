/**
 * 
 */
package dsg.roundagwt.gui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author slotm
 *
 */
public class TextDialog extends DialogBox implements HasText {

    private static TextDialogUiBinder uiBinder = GWT
            .create(TextDialogUiBinder.class);

    interface TextDialogUiBinder extends UiBinder<Widget, TextDialog> {
    }
    
    @UiField
    SimGUIResources res;
    
    @UiField
    TextArea textBox;

    public TextDialog() {
        setWidget(uiBinder.createAndBindUi(this));
        res.style().ensureInjected();
        setAutoHideEnabled(true);
        setAnimationEnabled(true);
        setGlassEnabled(true);
        addStyleName(res.style().outText());
    }

    public String getText() {
        return textBox.getValue();
    }
    
    public void setText(String text) {
        textBox.setValue(text, false);
    }
}
