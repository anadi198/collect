package org.odk.collect.android.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/** A ListPreference where each item has a caption and the entire dialog also has a caption. */
public class CaptionedListPreference extends ListPreference {
    private final Context context;
    private CharSequence[] captions;
    private String dialogCaption;

    private List<RadioButton> radioButtons;
    private ViewGroup listView;
    private TextView captionView;
    private int clickedIndex = -1;

    public CaptionedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setDialogLayoutResource(R.layout.captioned_list_dialog);
    }

    /** Sets the values, labels, and captions for the items in the dialog. */
    public void setItems(CharSequence[] values, CharSequence[] labels, CharSequence[] captions) {
        setEntryValues(values != null ? values : new CharSequence[0]);
        setEntries(labels != null ? labels : values);
        setCaptions(captions != null ? captions : new CharSequence[values.length]);
    }

    /** Sets the list of items to offer as choices in the dialog. */
    public void setCaptions(CharSequence[] captions) {
        this.captions = captions;
    }

    /** Sets the caption to show at the bottom of the dialog. */
    public void setDialogCaption(String dialogCaption) {
        this.dialogCaption = dialogCaption;
    }

    @Override protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // We aren't using the AlertDialog.Builder; we're inflating a custom dialog.
    }

    /** When the dialog is constructed, locates certain useful views in the dialog. */
    @Override protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        listView = view.findViewById(R.id.list);
        captionView = view.findViewById(R.id.dialog_caption);
        updateContent();
    }

    /** Updates the contents of the dialog to show the items passed in by setItems etc. */
    public void updateContent() {
        CharSequence[] values = getEntryValues();
        CharSequence[] labels = getEntries();

        if (listView != null && values != null && labels != null && captions != null) {
            listView.removeAllViews();
            radioButtons = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                RadioButton button = inflateItem(listView, i, values[i], labels[i], captions[i]);
                radioButtons.add(button);
            }
        }
        if (captionView != null) {
            captionView.setText(dialogCaption);
        }
    }

    /** Creates the view for an item in the list. */
    protected RadioButton inflateItem(ViewGroup parent, final int i, Object value, Object label, Object caption) {
        LinearLayout item = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.captioned_item, null);
        RadioButton button = item.findViewById(R.id.button);
        TextView labelView = item.findViewById(R.id.label);
        TextView captionView = item.findViewById(R.id.caption);
        labelView.setText(String.valueOf(label));
        captionView.setText(String.valueOf(caption));
        button.setChecked(ObjectUtils.equals(value, getSharedPreferences().getString(getKey(), null)));
        item.setOnClickListener(view -> onItemClicked(i));
        parent.addView(item);
        return button;
    }

    /** When an item is clicked, records which item, and updates the radio buttons. */
    protected void onItemClicked(int index) {
        clickedIndex = index;
        for (int i = 0; i < radioButtons.size(); i++) {
            radioButtons.get(i).setChecked(i == clickedIndex);
        }
    }

    /** Saves the selected value to the preferences when the dialog is closed. */
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult && clickedIndex >= 0) {
            Object value = getEntryValues()[clickedIndex];
            if (callChangeListener(value)) {
                setValue(value != null ? value.toString() : null);
            }
        }
    }
}
