package de.medieninf.mobcomp.scrapp.view;

import android.content.Context;
import android.widget.EditText;

/**
 * Class to save a action param id for an edit text.
 */
public class ActionParamEditText extends EditText {
    private int actionParamId;

    public ActionParamEditText(Context context){
        super(context);
    }

    public ActionParamEditText(Context context, int actionParamId) {
        super(context);
        this.actionParamId = actionParamId;
    }

    public int getActionParamId() {
        return actionParamId;
    }
}
