package com.artursworld.nccn.view.user;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artursworld.nccn.R;
import com.artursworld.nccn.controller.util.Global;
import com.artursworld.nccn.controller.util.Share;
import com.artursworld.nccn.controller.util.Strings;
import com.artursworld.nccn.model.entity.AbstractQuestionnaire;
import com.artursworld.nccn.model.persistence.manager.DistressThermometerQuestionnaireManager;
import com.artursworld.nccn.model.persistence.manager.EntityDbManager;
import com.artursworld.nccn.model.persistence.manager.HADSDQuestionnaireManager;
import com.artursworld.nccn.model.persistence.manager.QualityOfLifeManager;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class UserStartConfiguration {

    public static String CLASS_NAME = UserStartConfiguration.class.getSimpleName();

    // Attributes
    private MaterialDialog dialog;
    private Activity activity = null;

    public UserStartConfiguration(Activity context) {
        activity = context;
    }

    public void showConfigurationDialog() {
        if(dialog == null) {
            MaterialDialog.Builder b = new MaterialDialog.Builder(activity)
                    .title(R.string.configuration)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .customView(R.layout.dialog_select_questionnairies, true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            // set shared preferences
                            setQuestionnairesToBeDisplayedOnStartScreen(dialog);

                            Log.e(CLASS_NAME, "Default Questionnaire = "+Global.hasToUseDefaultQuestionnaire()+"");
                            Log.e(CLASS_NAME, "Create new Questionnaire = "+Global.hasToCreateNewQuestionnaire()+"");
                            Log.e(CLASS_NAME, "Create new User = "+Global.hasToCreateNewUser()+"");

                            // refresh and finish();
                            activity.finish();
                            activity.startActivity(activity.getIntent());

                        }
                    });
            dialog = b.build();
            MaterialDialog d = setCustomTextSize(25);
            setSelectedQestionnairesAsCheckedBeforeDialogShowUp(d);
            toggleById(R.id.toggle_user_layout, R.id.layout_users, R.string.select_existing_user, R.string.switch_to_new_user, R.string.create_new_user, R.string.switch_to_existing_user);
            toggleById(R.id.included_questionnaire_title, R.id.layout_below_included_questionnaire_title, R.string.configurable_questionnaire, R.string.switch_to_default_selection, R.string.standard_questionnaire, R.string.switch_to_questionnaire_selection);
            dialog = d;
        }
        dialog.show();
    }

    @NonNull
    private MaterialDialog setCustomTextSize(int textSize) {
        MaterialDialog d = dialog;
        d.getTitleView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize+ 5);
        d.getActionButton(DialogAction.POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        d.getActionButton(DialogAction.NEGATIVE).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        return d;
    }

    private void setSelectedQestionnairesAsCheckedBeforeDialogShowUp(MaterialDialog dialog) {
        CheckBox checkbox1 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_1);
        CheckBox checkbox2 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_2);
        CheckBox checkbox3 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_3);
        CheckBox checkbox4 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_4);

        Set<String> setOfBooleans = Global.getSelectedQuestionnairesForStartScreen();
        if (setOfBooleans != null) {
            if (!setOfBooleans.contains(Strings.getStringByRId(R.string.hadsd_questionnaire))) {
                checkbox1.setChecked(false);
            }
            if (!setOfBooleans.contains(Strings.getStringByRId(R.string.nccn_distress_thermometer))) {
                checkbox2.setChecked(false);
            }
            if (!setOfBooleans.contains(Strings.getStringByRId(R.string.quality_of_life_questionnaire))) {
                checkbox3.setChecked(false);
            }
            if (!setOfBooleans.contains(Strings.getStringByRId(R.string.fear_of_progression_questionnaire))) {
                checkbox4.setChecked(false);
            }
        }
    }

    /**
     * Sets the quesitonnares to be displayed on start screen via shared preferences
     *
     * @param dialog the dialog displaying the UI
     */
    private void setQuestionnairesToBeDisplayedOnStartScreen(@NonNull MaterialDialog dialog) {
        CheckBox checkbox1 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_1);
        CheckBox checkbox2 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_2);
        CheckBox checkbox3 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_3);
        CheckBox checkbox4 = (CheckBox) dialog.getView().findViewById(R.id.questionnaire_4);

        Set<String> selectedQuestionnairesSet = new HashSet<String>();
        if (checkbox1.isChecked()) {
            selectedQuestionnairesSet.add(Strings.getStringByRId(R.string.hadsd_questionnaire));
        }
        if (checkbox2.isChecked()) {
            selectedQuestionnairesSet.add(Strings.getStringByRId(R.string.nccn_distress_thermometer));
        }
        if (checkbox3.isChecked()) {
            selectedQuestionnairesSet.add(Strings.getStringByRId(R.string.quality_of_life_questionnaire));
        }
        if (checkbox4.isChecked()) {
            selectedQuestionnairesSet.add(Strings.getStringByRId(R.string.fear_of_progression_questionnaire));
        }
        Log.i(CLASS_NAME, "Following questionnaires are selected to be displayed on start screen=" + selectedQuestionnairesSet);
        Share.putStringSet(Strings.getStringByRId(R.string.c_selected_questionnaires), selectedQuestionnairesSet);
    }

    /**
     * Adds toggle mechanism to a expandable title view with subtitle and toggle button
     * and sets the displayed texts of the titles
     *
     * @param barToggleLayoutId            the id of included title, subtitle and switcher layout
     * @param layoutIdBelowBarToggleLayout the id of layout below included title, subtitle and switcher layout
     * @param titleTextId                  the title displayed if toggle button is checked
     * @param subTitleTextId               the subtitle displayed if toggle button is checked
     * @param invisibleTitleTextId         the title displayed if toggle button is unchecked
     * @param invisibleSubTitleTextId      the subtitle displayed if toggle button is unchecked
     */
    private void toggleById(final int barToggleLayoutId, final int layoutIdBelowBarToggleLayout, final int titleTextId, final int subTitleTextId, final int invisibleTitleTextId, final int invisibleSubTitleTextId) {
        final View toggleTitleLayout = dialog.getView().findViewById(barToggleLayoutId);
        Switch questionnaireSwitch = (Switch) toggleTitleLayout.findViewById(R.id.switcher);

        setSwitcherCheckedByResourceId(barToggleLayoutId, questionnaireSwitch);

        // init UI
        final TextView title = (TextView) toggleTitleLayout.findViewById(R.id.title);
        final TextView subtitle = (TextView) toggleTitleLayout.findViewById(R.id.subtitle);
        title.setText(Strings.getStringByRId(invisibleTitleTextId));
        subtitle.setText(Strings.getStringByRId(invisibleSubTitleTextId));

        final RelativeLayout layout = (RelativeLayout) dialog.getView().findViewById(layoutIdBelowBarToggleLayout);
        setUITextByCheckedSwitches(questionnaireSwitch.isChecked(), layout, title, titleTextId, subtitle, subTitleTextId, barToggleLayoutId, invisibleTitleTextId, invisibleSubTitleTextId);

        questionnaireSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setUITextByCheckedSwitches(isChecked, layout, title, titleTextId, subtitle, subTitleTextId, barToggleLayoutId, invisibleTitleTextId, invisibleSubTitleTextId);
            }
        });
    }

    private void setUITextByCheckedSwitches(boolean isChecked, RelativeLayout layout, TextView title, int titleTextId, TextView subtitle, int subTitleTextId, int barToggleLayoutId, int invisibleTitleTextId, int invisibleSubTitleTextId) {
        // show details UI
        if (isChecked) {
            layout.setVisibility(View.VISIBLE);
            title.setText(Strings.getStringByRId(titleTextId));
            subtitle.setText(Strings.getStringByRId(subTitleTextId));
            onToggleIsChecked(barToggleLayoutId);
        }
        // hide details
        else {
            layout.setVisibility(View.GONE);
            title.setText(Strings.getStringByRId(invisibleTitleTextId));
            subtitle.setText(Strings.getStringByRId(invisibleSubTitleTextId));
            onToggleIsUnchecked(barToggleLayoutId);
        }
    }

    /**
     * Set the switcher as checked by global db value. It checks if a user has to be created and
     * if the default questionnaires need to be displayed by the layout id
     * @param barToggleLayoutId the layout id of the UI element
     * @param switcher the switcher to be checked or not
     */
    private void setSwitcherCheckedByResourceId(int barToggleLayoutId, Switch switcher) {
        if(barToggleLayoutId == R.id.toggle_user_layout){
            boolean hasToCreateNewUser = !Global.hasToCreateNewUser();
            Global.setHasToCreateNewUser(hasToCreateNewUser);
            switcher.setChecked(hasToCreateNewUser);
        }
        else if(barToggleLayoutId == R.id.included_questionnaire_title){
            boolean hasToUseDefaultQuestionnaire = !Global.hasToUseDefaultQuestionnaire();
            Global.setHasToUseDefaultQuestionnaire(hasToUseDefaultQuestionnaire);
            switcher.setChecked(hasToUseDefaultQuestionnaire);
        }
    }

    private void onToggleIsUnchecked(int resourceId) {
        String resource = "";
        if(resourceId == R.id.toggle_user_layout){
            Global.setHasToCreateNewUser(true);
            Global.setHasToCreateNewQuestionnaire(true);
            Global.setSelectedQuestionnaireDate(new Date());
            resource = "setHasToCreateNewUser(true)";
        }
        else if(resourceId == R.id.included_questionnaire_title){
            Global.setHasToUseDefaultQuestionnaire(true);
            resource = "setHasToUseDefaultQuestionnaire(true)";
        }
        //Log.i(CLASS_NAME, resource + " .on toggle is unchecked");
    }

    private void onToggleIsChecked(int resourceId) {
        String resource = "";
        if(resourceId == R.id.toggle_user_layout){
            Global.setHasToCreateNewUser(false);
            Global.setHasToCreateNewQuestionnaire(false);
            resource = "setHasToCreateNewUser(false)";
        }
        else if(resourceId == R.id.included_questionnaire_title){
            Global.setHasToUseDefaultQuestionnaire(false);
            resource = "setHasToUseDefaultQuestionnaire(false)";
        }
        Log.i(CLASS_NAME, resource + " .on toggle is checked");
    }
}
