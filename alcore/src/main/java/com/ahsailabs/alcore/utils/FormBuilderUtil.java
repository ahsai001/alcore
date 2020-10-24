package com.ahsailabs.alcore.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.ahsailabs.alcore.R;
import com.ahsailabs.alcore.models.FormArgumentModel;
import com.ahsailabs.alcore.models.FormPropertiesModel;
import com.ahsailabs.alcore.models.FormValidationRuleModel;
import com.ahsailabs.alcore.models.FormViewJsonModel;
import com.ahsailabs.alcore.models.FormWidgetModel;
import com.ahsailabs.alcore.views.CustomVerticalStepper;
import com.ahsailabs.alutils.CommonUtil;
import com.ahsailabs.alutils.DataUtil;
import com.ahsailabs.alutils.DateStringUtil;
import com.ahsailabs.alutils.DebugUtil;
import com.ahsailabs.alutils.FormCommonUtil;
import com.ahsailabs.alutils.FormValidationUtil;
import com.ahsailabs.alutils.ViewEnablerUtil;
import com.ahsailabs.alutils.ViewUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

/**
 * Created by ahsai on 5/29/2018.
 */

public class FormBuilderUtil implements VerticalStepperForm{
    public static final String PAGE_TYPE_STEPPER = "stepper";
    public static final String PAGE_TYPE_SCROLL = "scroll";
    public static final String PAGE_TYPE_LINEAR = "linear";
    private CircleImageView formImageView;
    private View separatorView;
    private TextView formTitleView;
    private TextView formDescView;
    private NestedScrollView formScrollPanel;
    private LinearLayout formScrollContainer;
    private LinearLayout formLinearPanel;
    private CustomVerticalStepper formStepperPanel;
    private Activity activity;
    private ViewGroup rootView;
    private ViewGroup parentView;
    private LayoutInflater layoutInflater;
    private FormViewJsonModel formViewJsonModel;
    private FormValidationUtil formValidationUtil;
    private int validationType = FormValidationUtil.DefaultType;
    private Bundle savedInstanceState;

    public FormBuilderUtil(Activity activity, Bundle savedInstanceState) {
        this.activity = activity;
        this.savedInstanceState = savedInstanceState;
        init();
    }


    public void onSavedInstanceState(Bundle outState){
        //do nothing
    }

    private void init(){
        layoutInflater = LayoutInflater.from(activity);
        formValidationUtil = new FormValidationUtil(activity);
        initDefaultWidget();
    }

    public View getRootView(){
        return rootView;
    }


    public FormBuilderUtil withParentView(ViewGroup parentView){
        this.parentView = parentView;
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.base_form_layout, parentView, false);
        formImageView = rootView.findViewById(R.id.form_imageview);
        formTitleView = rootView.findViewById(R.id.form_titleview);
        formDescView = rootView.findViewById(R.id.form_descview);
        separatorView = rootView.findViewById(R.id.form_separatorview);
        formScrollPanel = rootView.findViewById(R.id.form_scroll_panel);
        formScrollContainer = rootView.findViewById(R.id.form_scroll_container);
        formStepperPanel = rootView.findViewById(R.id.form_stepper_panel);
        formLinearPanel = rootView.findViewById(R.id.form_linear_panel);
        return this;
    }

    public FormBuilderUtil withSavedInstanceState(Bundle savedInstanceState){
        this.savedInstanceState = savedInstanceState;
        return this;
    }

    public FormValidationUtil getFormValidationUtils(){
        return formValidationUtil;
    }


    public FormBuilderUtil withViewJson(String viewJson){
        Gson gson = new Gson();
        formViewJsonModel = gson.fromJson(viewJson, FormViewJsonModel.class);
        return this;
    }

    public FormBuilderUtil withViewJson(FormViewJsonModel viewJsonModel){
        formViewJsonModel = viewJsonModel;
        return this;
    }

    public FormBuilderUtil withViewJsonEmpty(String pageTitle, String pageType, String logo, String formTitle, String formDesc){
        formViewJsonModel = new FormViewJsonModel();
        formViewJsonModel.setPageTitle(pageTitle);
        formViewJsonModel.setPageType(pageType);
        formViewJsonModel.setFormTitle(formTitle);
        formViewJsonModel.setFormDesc(formDesc);
        formViewJsonModel.setLogo(logo);
        formViewJsonModel.setFormList(new ArrayList<FormWidgetModel>());
        return this;
    }

    public FormBuilderUtil setValidationType(int validationType) {
        //this config just for linear and scroll type, stepper ignore it
        this.validationType = validationType;
        return this;
    }

    public String getPageType(){
        return formViewJsonModel.getPageType();
    }



    private String getWidgetName(String widgetId){
        String result = null;
        ArrayList<FormWidgetModel> formWidgetModels = formViewJsonModel.getFormList();
        for (FormWidgetModel formWidgetModel : formWidgetModels){
            if(formWidgetModel.getId().equalsIgnoreCase(widgetId)){
                result = formWidgetModel.getWidgetName();
                break;
            }
        }
        return result;
    }

    private int getWidgetPosition(String widgetId){
        int result = 0;
        ArrayList<FormWidgetModel> formWidgetModels = formViewJsonModel.getFormList();
        for (int x=0;x<formWidgetModels.size();x++){
            if(formWidgetModels.get(x).getId().equalsIgnoreCase(widgetId)){
                result = x;
                break;
            }
        }
        return result;
    }

    public List<String> getWidgetIdsForWidget(String widgetName) {
        ArrayList<FormWidgetModel> formWidgetModels = formViewJsonModel.getFormList();
        ArrayList<String> widgetIdsResult = new ArrayList<>();
        for(FormWidgetModel formWidgetModel : formWidgetModels){
            if(formWidgetModel.getWidgetName().equalsIgnoreCase(widgetName)){
                widgetIdsResult.add(formWidgetModel.getId());
            }
        }
        return widgetIdsResult;
    }

    public List<FormWidgetModel> getAllValuableWidgets(){
        List<FormWidgetModel> resultModelList = new ArrayList<>();
        for (FormWidgetModel formWidgetModel : formViewJsonModel.getFormList()){
            Object value = getValueForWidget(formWidgetModel.getId());
            if(value != null){
                resultModelList.add(formWidgetModel);
            }
        }
        return resultModelList;
    }

    public Map<String, String> getAllValueIDMap(){
        Map<String, String> resultMap = new HashMap<>();
        for (FormWidgetModel formWidgetModel : formViewJsonModel.getFormList()){
            Object value = getValueForWidget(formWidgetModel.getId());
            if(value != null){
                resultMap.put(formWidgetModel.getFieldName(),(String)value);
            }
        }
        return resultMap;
    }

    public String getUrlQueryStringOfAll(){
        Map<String, String> resultMap = getAllValueIDMap();
        return DataUtil.mapToString(resultMap);
    }

    public View getLastViewForWidget(String widgetName){
        List<String> widgetIds = getWidgetIdsForWidget(widgetName);
        if(widgetIds.size() > 0){
            return getViewForWidget(widgetIds.get(widgetIds.size()-1));
        }
        return null;
    }

    public View getLastValuableView(){
        for (int i=formViewJsonModel.getFormList().size()-1;i>=0;i--){
            View valuableView = getValuableViewForWidget(i);
            if(valuableView != null){
                return valuableView;
            }
        }
        return null;
    }

    public void setAllValuableViewWithImeNext(){
        for (int i=0;i<formViewJsonModel.getFormList().size();i++){
            View valuableView = getValuableViewForWidget(i);
            if(valuableView != null && valuableView instanceof TextView){
                ((TextView)valuableView).setImeOptions(EditorInfo.IME_ACTION_NEXT);
            }
        }
    }

    public void setAllValuableViewWithData(JSONObject jsonObject){
        for (int i=0;i<formViewJsonModel.getFormList().size();i++){
            String widgetName = formViewJsonModel.getFormList().get(i).getWidgetName();
            String fieldName = formViewJsonModel.getFormList().get(i).getFieldName();
            View valuableView = getValuableViewForWidget(i);
            if(valuableView != null){
               widgetBuilderMap.get(widgetName).setWidgetValue(valuableView, jsonObject.opt(fieldName));
            }
        }
    }

    public void setLastValuableViewWithImeDone(){
        View valuableView = getLastValuableView();
        if(valuableView != null && valuableView instanceof TextView){
            ((TextView)valuableView).setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
    }


    // ======== Start of Get Root View of Widget ========== //
    public View getViewForWidget(String widgetId) {
        return rootView.findViewWithTag(widgetId);
    }


    public View getViewForWidget(int position) {
        String widgetId = formViewJsonModel.getFormList().get(position).getId();
        return getViewForWidget(widgetId);
    }
    // ======== End of Get Root View of Widget ========== //


    // =======
    public View getErrorViewForWidget(View widgetView, String widgetId){
        String widgetName = getWidgetName(widgetId);
        int viewIdForError = widgetBuilderMap.get(widgetName).getViewIdForError();
        return widgetView.findViewById(viewIdForError);
    }
    // ========

    // ======== Start of Get Valuable View of Widget ========== //

    public View getValuableViewForWidget(String widgetId) {
        View widgetView = getViewForWidget(widgetId);
        String widgetName = getWidgetName(widgetId);
        int viewIdForValue = widgetBuilderMap.get(widgetName).getViewIdForValue();
        View valuableView = widgetView.findViewById(viewIdForValue);
        if(valuableView == null){
            int position = getWidgetPosition(widgetId);
            valuableView = widgetView.findViewById(viewIdForValue+position+POSITION_DELTA);
        }
        return valuableView;
    }

    public View getValuableViewForWidget(View widgetView, String widgetId) {
        String widgetName = getWidgetName(widgetId);
        int viewIdForValue = widgetBuilderMap.get(widgetName).getViewIdForValue();
        View valuableView = widgetView.findViewById(viewIdForValue);
        if(valuableView == null){
            int position = getWidgetPosition(widgetId);
            valuableView = widgetView.findViewById(viewIdForValue+position+POSITION_DELTA);
        }
        return valuableView;
    }

    public View getValuableViewForWidget(int position) {
        View widgetView = getViewForWidget(position);
        FormWidgetModel formWidgetModel = formViewJsonModel.getFormList().get(position);
        int viewIdForValue = widgetBuilderMap.get(formWidgetModel.getWidgetName()).getViewIdForValue();
        View valuableView = widgetView.findViewById(viewIdForValue);
        if(valuableView == null){
            valuableView = widgetView.findViewById(viewIdForValue+position+POSITION_DELTA);
        }
        return valuableView;
    }

    public View getValuableViewForWidget(View widgetView, int position) {
        FormWidgetModel formWidgetModel = formViewJsonModel.getFormList().get(position);
        int viewIdForValue = widgetBuilderMap.get(formWidgetModel.getWidgetName()).getViewIdForValue();
        View valuableView = widgetView.findViewById(viewIdForValue);
        if(valuableView == null){
            valuableView = widgetView.findViewById(viewIdForValue+position+POSITION_DELTA);
        }
        return valuableView;
    }

    // ======== End of Get Valuable View of Widget ========== //


    // ======== Start of Get Value of Widget ========== //

    public Object getValueForWidget(String widgetId) {
        View widgetView = getViewForWidget(widgetId);
        String widgetName = getWidgetName(widgetId);
        int viewIdForValue = widgetBuilderMap.get(widgetName).getViewIdForValue();
        View widgetValuableView = widgetView.findViewById(viewIdForValue);
        if(widgetValuableView == null){
            int position = getWidgetPosition(widgetId);
            widgetValuableView = widgetView.findViewById(viewIdForValue+position+POSITION_DELTA);
        }
        return widgetBuilderMap.get(widgetName).getWidgetValue(widgetValuableView);
    }

    public Object getValueForWidget(int position) {
        View widgetView = getViewForWidget(position);
        FormWidgetModel formWidgetModel = formViewJsonModel.getFormList().get(position);
        int viewIdForValue = widgetBuilderMap.get(formWidgetModel.getWidgetName()).getViewIdForValue();
        View widgetValuableView = widgetView.findViewById(viewIdForValue);
        if(widgetValuableView == null){
            widgetValuableView = widgetView.findViewById(viewIdForValue+position+POSITION_DELTA);
        }
        return widgetBuilderMap.get(formWidgetModel.getWidgetName()).getWidgetValue(widgetValuableView);
    }

    // ======== End of Get Value of Widget ========== //


    public void setEnablerIfMandatoryDoneOnView(View targetView){
        //need run setValidationType(FormValidationUtils.IdleType)
        final ViewEnablerUtil viewEnablerUtil = new ViewEnablerUtil(targetView, 0);
        FormValidationUtil formValidationUtils = getFormValidationUtils();
        int targetReportTotal = 0;
        for (int x=0;x<formValidationUtils.getValidatorCount();x++){
            FormValidationUtil.AbstractValidator validator = formValidationUtils.getValidator(x);
            for (int y=0;y<validator.getRuleCount();y++){
                if(validator.getRule(y) instanceof FormValidationUtil.NotEmptyValidatorRule){
                    validator.addOnValidationCallback(new FormValidationUtil.OnValidationCallback() {
                        @Override
                        public boolean onSuccess(View view, FormValidationUtil.ValidatorRuleInterface validatorRule) {
                            return false;
                        }

                        @Override
                        public boolean onFailed(View view, FormValidationUtil.ValidatorRuleInterface validatorRule, String message) {
                            return false;
                        }

                        @Override
                        public void onComplete(View view, boolean allRuleValid) {
                            if(allRuleValid){
                                viewEnablerUtil.done(String.valueOf(view.getId()));
                            } else {
                                viewEnablerUtil.unDone(String.valueOf(view.getId()));
                            }
                        }
                    }).setAlwaysShowErrorOnView(true);
                    targetReportTotal++;
                }
            }
        }
        viewEnablerUtil.setTargetedDoneTotal(targetReportTotal).init();
    }


    public static class WidgetBuilder {
        FormBuilderUtil formBuilderUtil;
        FormWidgetModel formWidgetModel;
        public WidgetBuilder(String widgetName, String id){
            formWidgetModel = new FormWidgetModel();
            formWidgetModel.setWidgetName(widgetName);
            formWidgetModel.setId(id);
            formWidgetModel.setProperties(new ArrayList<FormPropertiesModel>());
            formWidgetModel.setValidation(new ArrayList<FormValidationRuleModel>());
        }

        private void setFormBuilderUtil(FormBuilderUtil formBuilderUtil){
            this.formBuilderUtil = formBuilderUtil;
        }

        public void addToFormBuilder(){
            this.formBuilderUtil.formViewJsonModel.getFormList().add(formWidgetModel);
        }
        public WidgetBuilder setId(String id){
            formWidgetModel.setId(id);
            return this;
        }
        public WidgetBuilder setLabel(String label){
            formWidgetModel.setLabel(label);
            return this;
        }
        public WidgetBuilder setSubLabel(String subLabel){
            formWidgetModel.setSubLabel(subLabel);
            return this;
        }

        public WidgetBuilder setProperty(String propertyKey, ArgumentValueList propertyList){
            FormPropertiesModel formPropertiesModel = new FormPropertiesModel();
            formPropertiesModel.setPropKey(propertyKey);
            ArrayList<FormArgumentModel> propertiesArgModelList = new ArrayList<>();
            for(Map.Entry<String, Object> arg: propertyList.getArguments().entrySet()){
                FormArgumentModel formArgumentModel = new FormArgumentModel();
                formArgumentModel.setArgType(arg.getKey());
                formArgumentModel.setArgValue(arg.getValue());
                propertiesArgModelList.add(formArgumentModel);
            }
            formPropertiesModel.setPropArg(propertiesArgModelList);
            formWidgetModel.getProperties().add(formPropertiesModel);
            return this;
        }

        public WidgetBuilder setValidationRule(String ruleName, String errorMessage, ArgumentValueList argumentValueList){
            FormValidationRuleModel formValidationRuleModel = new FormValidationRuleModel();
            formValidationRuleModel.setRuleName(ruleName);
            formValidationRuleModel.setErrorMessage(errorMessage);
            ArrayList<FormArgumentModel> argModelList = new ArrayList<>();
            for(Map.Entry<String, Object> arg: argumentValueList.getArguments().entrySet()){
                FormArgumentModel formArgumentModel = new FormArgumentModel();
                formArgumentModel.setArgType(arg.getKey());
                formArgumentModel.setArgValue(arg.getValue());
                argModelList.add(formArgumentModel);
            }
            formValidationRuleModel.setRuleArgs(argModelList);
            formWidgetModel.getValidation().add(formValidationRuleModel);
            return this;
        }
    }


    public WidgetBuilder createWidget(String widgetName, String id){
        WidgetBuilder widgetBuilder = new WidgetBuilder(widgetName, id);
        widgetBuilder.setFormBuilderUtil(this);
        return widgetBuilder;
    }


    public static class ArgumentValueList {
        HashMap<String, Object> arguments;
        public ArgumentValueList(){
            arguments = new HashMap<>();
        }

        public ArgumentValueList add(String propType, Object propValue){
            arguments.put(propType, propValue);
            return this;
        }

        public HashMap<String, Object> getArguments(){
            return arguments;
        }
    }


    public void show(){
        parentView.addView(rootView);
    }

    public FormBuilderUtil render(){
        String pageTitle = formViewJsonModel.getPageTitle();
        String pageType = formViewJsonModel.getPageType();
        String logo = formViewJsonModel.getLogo();
        String formtitle = formViewJsonModel.getFormTitle();
        String formdesc = formViewJsonModel.getFormDesc();
        ArrayList<FormWidgetModel> widgetList = formViewJsonModel.getFormList();

        if(activity != null){
            ((AppCompatActivity)activity).getSupportActionBar().setTitle(pageTitle);
        }

        boolean needShowSeparator = false;

        if(!TextUtils.isEmpty(logo) && URLUtil.isValidUrl(logo)) {
            Picasso.get().load(logo).into(formImageView);
            needShowSeparator = true;
            formImageView.setVisibility(View.VISIBLE);
        } else {
            formImageView.setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(formtitle)) {
            formTitleView.setText(formtitle);
            needShowSeparator = true;
            formTitleView.setVisibility(View.VISIBLE);
        } else {
            formTitleView.setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(formdesc)) {
            formDescView.setText(formdesc);
            needShowSeparator = true;
            formDescView.setVisibility(View.VISIBLE);
        } else {
            formDescView.setVisibility(View.GONE);
        }

        separatorView.setVisibility(needShowSeparator?View.VISIBLE:View.GONE);

        ViewGroup viewContainer = getViewContainer(pageType);

        if(viewContainer instanceof LinearLayout) {
            setupLinearPanel(viewContainer, widgetList);
        } else  if(viewContainer instanceof CustomVerticalStepper) {
            setupStepperPanel(viewContainer, widgetList);
        }

        return this;
    }

    private View getItemView(ArrayList<FormWidgetModel> widgetList, int position){
        FormWidgetModel formWidgetModel = widgetList.get(position);
        String widgetName = formWidgetModel.getWidgetName();
        View currentView = null;
        if(widgetBuilderMap.containsKey(widgetName)){
            currentView = getWidgetFromBuilder(activity, formWidgetModel, position);
        }
        return currentView;
    }

    private void setupLinearPanel(ViewGroup viewContainer, ArrayList<FormWidgetModel> widgetList){
        for (int i = 0; i < widgetList.size(); i++) {
            View currentView = getItemView(widgetList, i);
            if (currentView != null) {
                viewContainer.addView(currentView);
            }
        }

        String[] titleSteps = new String[0];
        String[] subTitleSteps = new String[0];

        VerticalStepperFormLayout.Builder.newInstance(formStepperPanel, titleSteps, this, activity)
                .stepsSubtitles(subTitleSteps)
                .init();
    }

    private void setupStepperPanel(ViewGroup viewContainer, ArrayList<FormWidgetModel> widgetList){
        int colorPrimary = ContextCompat.getColor(activity, R.color.colorPrimary);
        int colorPrimaryDark = ContextCompat.getColor(activity, R.color.colorPrimaryDark);

        String[] titleSteps = new String[widgetList.size()];
        String[] subTitleSteps = new String[widgetList.size()];
        for (int i = 0; i < widgetList.size(); i++) {
            FormWidgetModel formWidgetModel = widgetList.get(i);
            titleSteps[i] = formWidgetModel.getLabel();
            subTitleSteps[i] = formWidgetModel.getSubLabel();
        }

        // Setting up and initializing the form
        VerticalStepperFormLayout.Builder.newInstance(formStepperPanel, titleSteps, this, activity)
                .stepsSubtitles(subTitleSteps)
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
                .displayBottomNavigation(true)
                .materialDesignInDisabledSteps(true)
                .showVerticalLineWhenStepsAreCollapsed(true)
                .init();


        formStepperPanel.makeTitleMultiLine();

        formStepperPanel.setActionOnTitleAndSubTitle(new CustomVerticalStepper.HeadClickListener() {
            @Override
            public void onClick(int stepNumber) {

            }
        });
    }

    private int POSITION_DELTA = 1000;//to solved same view id in the same hirarchy

    private View getWidgetFromBuilder(Context context, FormWidgetModel formWidgetModel, int position){
        String widgetID = formWidgetModel.getId();
        String widgetName = formWidgetModel.getWidgetName();

        List<Class> typeList = new ArrayList<>();
        List<Object> valueList = new ArrayList<>();
        loadArgument(context, formWidgetModel.getData(), typeList, valueList);

        View widgetView = widgetBuilderMap.get(widgetName).getWidgetView(context,layoutInflater,rootView, valueList);

        List<FormPropertiesModel> formPropertiesModels = formWidgetModel.getProperties();
        implementProperties(widgetView, widgetName, formPropertiesModels);

        List<FormValidationRuleModel> formValidationRuleModels = formWidgetModel.getValidation();
        implementValidation(widgetBuilderMap.get(widgetName), widgetView, widgetID, formValidationRuleModels, position);

        setupLabelView(widgetView, widgetName,formWidgetModel.getLabel()+(isWidgetMandatory(formValidationRuleModels)?"*":""));
        setupSubLabelView(widgetView, widgetName,formWidgetModel.getSubLabel());

        widgetView.setTag(widgetID);//mandatory

        View valuableView = getValuableViewForWidget(widgetView,widgetID);
        if(valuableView != null){
            valuableView.setId(valuableView.getId()+position+POSITION_DELTA);
        }
        return widgetView;
    }

    private boolean isWidgetMandatory(List<FormValidationRuleModel> formValidationRuleModels){
        if(formValidationRuleModels == null) return false;
        for (FormValidationRuleModel formValidationRuleModel : formValidationRuleModels) {
            if(formValidationRuleModel.getRuleName().equalsIgnoreCase("notempty")){
                return true;
            }
        }
        return false;
    }

    private void setupLabelView(View widgetView, String widgetName, String label) {
        int viewIdLabelView = widgetBuilderMap.get(widgetName).getViewIdForLabel();
        View labelView = widgetView;
        if(viewIdLabelView > 0){
            labelView = widgetView.findViewById(viewIdLabelView);
        }
        if(labelView != null && label != null) {
            widgetBuilderMap.get(widgetName).setLabel(labelView, label);
        }
    }

    private void setupSubLabelView(View widgetView, String widgetName, String subLabel) {
        int viewIdSubLabelView = widgetBuilderMap.get(widgetName).getViewIdForSubLabel();
        View subLabelView = widgetView;
        if(viewIdSubLabelView > 0){
            subLabelView = widgetView.findViewById(viewIdSubLabelView);
        }
        if(subLabelView != null && subLabel != null) {
            widgetBuilderMap.get(widgetName).setSubLabel(subLabelView, subLabel);
        }
    }

    private View getCustomWidget(Context context, FormWidgetModel formWidgetModel){
        String widgetID = formWidgetModel.getId();
        String widgetName = formWidgetModel.getWidgetName();
        List<FormPropertiesModel> formPropertiesModels = formWidgetModel.getProperties();

        View customObject = null;
        try {
            Class customViewClass = Class.forName(widgetName);
            Constructor<?> constructor = customViewClass.getConstructor(Context.class);
            customObject = (View)constructor.newInstance(context);

            implementProperties(customObject, widgetName, formPropertiesModels);

            customObject.setId(widgetID.hashCode());
            customObject.setTag(widgetID);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return customObject;
    }


    private boolean handleCustomArgument(Context context, String argType, Object argValue, List<Class> typeList, List<Object> valueList){
        if(argumentsBuilderMap.containsKey(argType)){
            typeList.add(argumentsBuilderMap.get(argType).getType(argType));

            if(argValue == null){
                valueList.add(null);
            } else if(argValue instanceof String && TextUtils.isEmpty((String)argValue)) {
                valueList.add(null);
            } else {
                valueList.add(argumentsBuilderMap.get(argType).getValue(argValue));
            }
            return true;
        } else if(argType.equalsIgnoreCase("selectableitem")){
            Drawable drawable = ViewUtil.getSelectableItemBackgroundWithColor(context, Color.parseColor((String)argValue));
            typeList.add(Drawable.class);
            valueList.add(drawable);
            return true;
        } else if(argType.equalsIgnoreCase("selectableItemBorderLess")){
            Drawable drawable = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                drawable = ViewUtil.getSelectableItemBackgroundBorderLessWithColor(context, Color.parseColor((String)argValue));
            }
            typeList.add(Drawable.class);
            valueList.add(drawable);
            return true;
        }  else if(argType.equalsIgnoreCase("color")){
            typeList.add(int.class);
            valueList.add(Color.parseColor((String)argValue));
            return true;
        } else if(argType.equalsIgnoreCase("int")){
            typeList.add(int.class);
            if(argValue instanceof Double) {
                valueList.add(((Double) argValue).intValue());
            } else if(argValue instanceof Float) {
                valueList.add(((Float) argValue).intValue());
            } else if(argValue instanceof Integer) {
                valueList.add((int)argValue);
            } else {
                valueList.add(argValue);
            }
            return true;
        } else if(argType.equalsIgnoreCase("boolean")){
            typeList.add(boolean.class);
            valueList.add((boolean)argValue);
            return true;
        } else if(argType.equalsIgnoreCase("string")){
            typeList.add(String.class);
            valueList.add((String)argValue);
            return true;
        }  else if(argType.equalsIgnoreCase("chars")){
            typeList.add(CharSequence.class);
            valueList.add((CharSequence)argValue);
            return true;
        } else if(argType.equalsIgnoreCase("double")){
            typeList.add(double.class);
            valueList.add((double)argValue);
            return true;
        } else if(argType.equalsIgnoreCase("float")){
            typeList.add(float.class);
            valueList.add((float)argValue);
            return true;
        } else if(argType.equalsIgnoreCase("viewwithwidgetid")){
            typeList.add(View.class);
            valueList.add(getValuableViewForWidget((String)argValue));
            return true;
        }

        return false;
    }

    private void loadArgument(Context context, List<FormArgumentModel> formArgumentModels, List<Class> typeList, List<Object> valueList){
        if(formArgumentModels == null)return;
        for (int x = 0; x < formArgumentModels.size(); x++) {
            String argType = formArgumentModels.get(x).getArgType();
            Object argValue = formArgumentModels.get(x).getArgValue();

            if (!handleCustomArgument(context, argType, argValue, typeList, valueList)) {
                try {
                    Class thisClass = Class.forName(argType);
                    if (Integer.class.equals(thisClass)) {
                        typeList.add(int.class);
                        if (argValue instanceof Double) {
                            valueList.add(((Double) argValue).intValue());
                        } else if (argValue instanceof Float) {
                            valueList.add(((Float) argValue).intValue());
                        } else if (argValue instanceof Integer) {
                            valueList.add((int) argValue);
                        } else {
                            valueList.add(argValue);
                        }
                    } else if (Boolean.class.equals(thisClass)) {
                        typeList.add(boolean.class);
                        valueList.add((boolean) argValue);
                    } else if (Double.class.equals(thisClass)) {
                        typeList.add(boolean.class);
                        valueList.add((double) argValue);
                    } else if (Float.class.equals(thisClass)) {
                        typeList.add(boolean.class);
                        valueList.add((float) argValue);
                    } else {
                        typeList.add(thisClass);
                        valueList.add(argValue);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void implementProperties(View targetView,String widgetName,List<FormPropertiesModel> formPropertiesModels){
        if(formPropertiesModels == null)return;
        for (FormPropertiesModel formPropertiesModel : formPropertiesModels){
            ArrayList<FormArgumentModel> formArgumentModels = formPropertiesModel.getPropArgs();

            List<Class> typeList = new ArrayList<>();
            List<Object> valueList = new ArrayList<>();
            loadArgument(targetView.getContext(), formArgumentModels, typeList, valueList);

            String propertyKey = formPropertiesModel.getPropKey();
            List<String> propertyMethods = null;
            List<Integer> viewIds = null;

            if(customPropertyBuilderMap.containsKey(widgetName+":"+propertyKey)){
                CustomPropertyFactory customPropertyFactory = customPropertyBuilderMap.get(widgetName+":"+propertyKey);
                viewIds = customPropertyFactory.getViewIds();
                List<View> viewList = new ArrayList<>();
                for (Integer viewId : viewIds){
                    View view = targetView.findViewById(viewId);
                    viewList.add(view);
                }
                customPropertyFactory.runPropertyMethod(targetView.getContext(),viewList,valueList);
            } else if(nativePropertyBuilderMap.containsKey(widgetName+":"+propertyKey)){
                NativePropertyFactory nativePropertyFactory = nativePropertyBuilderMap.get(widgetName+":"+propertyKey);
                viewIds = nativePropertyFactory.getViewIds();
                propertyMethods = nativePropertyFactory.getNativePropertyMethod(propertyKey);

                for(int i=0; i<viewIds.size(); i++){
                    View target = targetView.findViewById(viewIds.get(i));
                    runPropertyMethodWithReflection(target, widgetName, propertyMethods.get(i), typeList, valueList);
                }

            } else if(!propertyKey.startsWith(".")){
                if(!propertyKey.startsWith("set")) {
                    propertyKey = "set" + CommonUtil.toCamelCase(propertyKey);
                }

                runPropertyMethodWithReflection(targetView, widgetName, propertyKey, typeList, valueList);
            } else {
                propertyKey = propertyKey.replace(".","");
                runPropertyMethodWithReflection(targetView, widgetName, propertyKey, typeList, valueList);
            }
        }
    }


    private void runPropertyMethodWithReflection(View object, String widgetName, String propertyMethod, List<Class> typeList, List<Object> valueList){
        try {
            Method method = object.getClass().getMethod(propertyMethod, typeList.toArray(new Class[typeList.size()]));
            method.invoke(object, valueList.toArray(new Object[valueList.size()]));
        } catch (IllegalAccessException e) {
            DebugUtil.logD("formbuilder", widgetName+"-IllegalAccessException:"+propertyMethod);
        } catch (InvocationTargetException e) {
            DebugUtil.logD("formbuilder", widgetName+"-InvocationTargetException:"+propertyMethod);
        } catch (NoSuchMethodException e) {
            DebugUtil.logD("formbuilder", widgetName+"-NoSuchMethodException:"+propertyMethod);
        }
    }

    private void implementValidation(WidgetFactory widgetFactory, View targetView, String widgetId, List<FormValidationRuleModel> formValidationRuleModels, final int position){
        if(formValidationRuleModels == null)return;
        if(formValidationRuleModels.size() > 0) {
            View valuableView = getValuableViewForWidget(targetView, widgetId);
            if(valuableView != null) {
                View errorView = getErrorViewForWidget(targetView, widgetId);
                FormValidationUtil.AbstractValidator validator = widgetFactory.getValidator(targetView.getContext(), valuableView, errorView, null);
                if (validator != null) {
                    for (FormValidationRuleModel formValidationRuleModel : formValidationRuleModels) {
                        ArrayList<FormArgumentModel> formArgumentModels = formValidationRuleModel.getRuleArgs();

                        List<Class> typeList = new ArrayList<>();
                        List<Object> valueList = new ArrayList<>();

                        loadArgument(targetView.getContext(), formArgumentModels, typeList, valueList);

                        String ruleName = formValidationRuleModel.getRuleName();
                        if (validationRuleBuilderMap.containsKey(ruleName)) {
                            FormValidationUtil.ValidatorRuleInterface validatorRule
                                    = validationRuleBuilderMap.get(ruleName).getValidationRule(targetView.getContext(), validator, typeList, valueList);
                            if (!TextUtils.isEmpty(formValidationRuleModel.getErrorMessage())) {
                                validatorRule.setErrorMessage(formValidationRuleModel.getErrorMessage());
                            }
                            validator.addRule(validatorRule);
                        }
                    }


                    if (formViewJsonModel.getPageType().equalsIgnoreCase(PAGE_TYPE_STEPPER)) {
                        validator.addOnValidationCallback(new FormValidationUtil.OnValidationCallback() {
                            @Override
                            public boolean onSuccess(View view, FormValidationUtil.ValidatorRuleInterface validatorRule) {
                                return false;
                            }

                            @Override
                            public boolean onFailed(View view, FormValidationUtil.ValidatorRuleInterface validatorRule, String message) {
                                formStepperPanel.setStepAsUncompleted(position, message);
                                return true;
                            }

                            @Override
                            public void onComplete(View view, boolean allRuleValid) {
                                if (allRuleValid) {
                                    formStepperPanel.setStepAsCompleted(position);
                                }
                            }
                        });
                    }
                    formValidationUtil.addValidator(validator);
                }
            }
        }
    }





    private ViewGroup getViewContainer(String pageType){
        if(pageType.equalsIgnoreCase(PAGE_TYPE_STEPPER) && activity != null){
            formStepperPanel.setVisibility(View.VISIBLE);
            return formStepperPanel;
        } else if(pageType.equalsIgnoreCase(PAGE_TYPE_LINEAR)){
            formLinearPanel.setVisibility(View.VISIBLE);
            return formLinearPanel;
        }

        //default is scroll
        formScrollPanel.setVisibility(View.VISIBLE);
        return formScrollContainer;
    }

    @Override
    public View createStepContentView(int stepNumber) {
        ArrayList<FormWidgetModel> widgetList = formViewJsonModel.getFormList();
        return getItemView(widgetList, stepNumber);
    }

    @Override
    public void onStepOpening(final int stepNumber) {
        if(formViewJsonModel.getPageType().equalsIgnoreCase(PAGE_TYPE_STEPPER)) {
            FormValidationUtil.AbstractValidator validator = formValidationUtil.getValidator(stepNumber);
            if (validator != null) {
                validator.validate();
            } else {
                formStepperPanel.setStepAsCompleted(stepNumber);
            }
        }
    }

    @Override
    public void sendData() {
        if(formViewJsonModel.getPageType().equalsIgnoreCase(PAGE_TYPE_STEPPER)) {
            if (onHandleCustomActionAtStepper != null) {
                onHandleCustomActionAtStepper.handleCustomAction(getUrlQueryStringOfAll(), getAllValueIDMap());
            }
        }
    }

    private OnHandleCustomActionAtStepper onHandleCustomActionAtStepper;

    public void setOnHandleCustomActionAtStepper(OnHandleCustomActionAtStepper onHandleCustomActionAtStepper) {
        this.onHandleCustomActionAtStepper = onHandleCustomActionAtStepper;
    }

    public interface OnHandleCustomActionAtStepper {
        public void handleCustomAction(String urlQueryString, Map<String, String> widgetIDValueMap);
    }


    private HashMap<String, WidgetFactory> widgetBuilderMap = new HashMap<>();
    public void registerWidgetFactory(String widgetName, WidgetFactory widgetBuilder){
        widgetBuilderMap.put(widgetName, widgetBuilder);
    }
    public static abstract class WidgetFactory {
        public abstract View getWidgetView(Context context, LayoutInflater layoutInflater, ViewGroup parentView, List<Object> data);
        public abstract int getViewIdForLabel();
        public abstract void setLabel(View labelView, String label);
        public abstract int getViewIdForSubLabel();
        public abstract void setSubLabel(View subLabelView, String subLabel);
        public abstract FormValidationUtil.AbstractValidator getValidator(Context context, View valuableView, View errorView, Object packet);
        public abstract int getViewIdForError();
        public abstract void setError(View errorView, String error);
        public abstract int getViewIdForValue();
        public abstract Object getWidgetValue(View widgetValuableView);
        public abstract void setWidgetValue(View widgetValuableView, Object value);
    }



    private HashMap<String, ArgumentFactory> argumentsBuilderMap = new HashMap<>();
    public void registerArgumentFactory(String argName, ArgumentFactory argumentFactory){
        argumentsBuilderMap.put(argName, argumentFactory);
    }
    public static abstract class ArgumentFactory {
        public abstract Class getType(String propType);
        public abstract Object getValue(Object propValue);
    }


    private HashMap<String, NativePropertyFactory> nativePropertyBuilderMap = new HashMap<>();
    public void registerNativePropertyFactory(String widgetName, String propertyKey, NativePropertyFactory nativePropertyFactory){
        nativePropertyBuilderMap.put(widgetName+":"+propertyKey, nativePropertyFactory);
    }
    public static abstract class NativePropertyFactory {
        public abstract List<Integer> getViewIds();
        public abstract List<String> getNativePropertyMethod(String propertyKey);
    }



    private HashMap<String, CustomPropertyFactory> customPropertyBuilderMap = new HashMap<>();
    public void registerCustomPropertyFactory(String widgetName, String propertyKey, CustomPropertyFactory customPropertyFactory){
        customPropertyBuilderMap.put(widgetName+":"+propertyKey, customPropertyFactory);
    }
    public static abstract class CustomPropertyFactory {
        public abstract List<Integer> getViewIds();
        public abstract Object runPropertyMethod(Context context, List<View> views, List<Object> arguments);
    }


    private HashMap<String, ValidationRuleFactory> validationRuleBuilderMap = new HashMap<>();
    public void registerValidationRuleFactory(String validationRuleyName, ValidationRuleFactory validationRuleFactory){
        validationRuleBuilderMap.put(validationRuleyName, validationRuleFactory);
    }
    public static abstract class ValidationRuleFactory {
        public abstract FormValidationUtil.ValidatorRuleInterface getValidationRule(Context context, FormValidationUtil.AbstractValidator validator, List<Class> typeList, List<Object> valueList);
    }





    private void initDefaultWidget(){
        //Widget Factory
        registerWidgetFactory("edittext", new WidgetFactory() {
            @Override
            public View getWidgetView(Context context, LayoutInflater layoutInflater, ViewGroup parentView, List<Object> data) {
                return layoutInflater.inflate(R.layout.base_form_textinput_edittext, parentView, false);
            }

            @Override
            public int getViewIdForLabel() {
                return R.id.widget_textinputlayout_edit;
            }

            @Override
            public void setLabel(View labelView, String label) {
                ((TextInputLayout)labelView).setHint(label);
            }

            @Override
            public int getViewIdForSubLabel() {
                return 0;
            }

            @Override
            public void setSubLabel(View subLabelView, String subLabel) {

            }

            @Override
            public FormValidationUtil.AbstractValidator getValidator(Context context, View valuableView, View errorView, Object packet) {
                return new FormValidationUtil.EditTextValidator(context, valuableView, errorView);
            }

            @Override
            public int getViewIdForError() {
                return R.id.widget_textinputlayout_edit;
            }

            @Override
            public void setError(View errorView, String error) {
                ((TextInputLayout)errorView).setError(error);
            }

            @Override
            public int getViewIdForValue() {
                return R.id.widget_edittext_edit;
            }

            @Override
            public Object getWidgetValue(View widgetValuableView) {
                return ((TextInputEditText)widgetValuableView).getText().toString();
            }

            @Override
            public void setWidgetValue(View widgetValuableView, Object value) {
                ((EditText)widgetValuableView).setText((String)value);
            }
        });

        registerWidgetFactory("edittext2", new WidgetFactory() {
            @Override
            public View getWidgetView(Context context, LayoutInflater layoutInflater, ViewGroup parentView, List<Object> data) {
                return layoutInflater.inflate(R.layout.base_form_label_edittext, parentView, false);
            }

            @Override
            public int getViewIdForLabel() {
                return R.id.base_form_edittext_labelView;
            }

            @Override
            public void setLabel(View labelView, String label) {
                ((TextView)labelView).setText(label);
            }

            @Override
            public int getViewIdForSubLabel() {
                return 0;
            }

            @Override
            public void setSubLabel(View subLabelView, String subLabel) {

            }

            @Override
            public FormValidationUtil.AbstractValidator getValidator(Context context, View valuableView, View errorView, Object packet) {
                return new FormValidationUtil.EditText2Validator(context, valuableView, errorView);
            }

            @Override
            public int getViewIdForError() {
                return R.id.base_form_edittext_edittextView;
            }

            @Override
            public void setError(View errorView, String error) {
                ((EditText)errorView).setError(error);
            }

            @Override
            public int getViewIdForValue() {
                return R.id.base_form_edittext_edittextView;
            }

            @Override
            public Object getWidgetValue(View widgetValuableView) {
                return ((EditText)widgetValuableView).getText().toString();
            }

            @Override
            public void setWidgetValue(View widgetValuableView, Object value) {
                ((EditText)widgetValuableView).setText((String)value);
            }
        });

        registerWidgetFactory("date", new WidgetFactory() {
            @Override
            public View getWidgetView(Context context, LayoutInflater layoutInflater, ViewGroup parentView, final List<Object> data) {
                View editTextLayout = layoutInflater.inflate(R.layout.base_form_label_edittext, parentView, false);
                EditText editText = editTextLayout.findViewById(getViewIdForValue());
                editText.setText(DateStringUtil.convertDateToString((String) data.get(1), Calendar.getInstance().getTime(), (TimeZone) data.get(2),(Locale) data.get(3)));
                ViewUtil.enableDatePicker(editText, (String) data.get(1), (TimeZone) data.get(2), (Locale) data.get(3),
                        ((AppCompatActivity) activity).getSupportFragmentManager(),(String)data.get(0),true, null, false);

                return editTextLayout;
            }

            @Override
            public int getViewIdForLabel() {
                return R.id.base_form_edittext_labelView;
            }

            @Override
            public void setLabel(View labelView, String label) {
                ((TextView)labelView).setText(label);
            }

            @Override
            public int getViewIdForSubLabel() {
                return 0;
            }

            @Override
            public void setSubLabel(View subLabelView, String subLabel) {

            }

            @Override
            public FormValidationUtil.AbstractValidator getValidator(Context context, View valuableView, View errorView, Object packet) {
                return new FormValidationUtil.EditTextValidator(context, valuableView, errorView);
            }

            @Override
            public int getViewIdForError() {
                return 0;
            }

            @Override
            public void setError(View errorView, String error) {

            }

            @Override
            public int getViewIdForValue() {
                return R.id.base_form_edittext_edittextView;
            }

            @Override
            public Object getWidgetValue(View widgetValuableView) {
                return ((EditText)widgetValuableView).getText().toString();
            }

            @Override
            public void setWidgetValue(View widgetValuableView, Object value) {
                ((EditText)widgetValuableView).setText((String)value);
            }
        });

        registerWidgetFactory("button", new WidgetFactory() {
            @Override
            public View getWidgetView(Context context, LayoutInflater layoutInflater, ViewGroup parentView, List<Object> data) {
                return layoutInflater.inflate(R.layout.base_form_button, parentView, false);
            }

            @Override
            public int getViewIdForLabel() {
                return R.id.base_form_button;
            }

            @Override
            public void setLabel(View labelView, String label) {
                ((Button)labelView).setText(label);
                ((Button)labelView).setAllCaps(false);
            }

            @Override
            public int getViewIdForSubLabel() {
                return 0;
            }

            @Override
            public void setSubLabel(View subLabelView, String subLabel) {

            }

            @Override
            public FormValidationUtil.AbstractValidator getValidator(Context context, View valuableView, View errorView, Object packet) {
                return null;
            }

            @Override
            public int getViewIdForError() {
                return 0;
            }

            @Override
            public void setError(View errorView, String error) {

            }

            @Override
            public int getViewIdForValue() {
                return 0;
            }

            @Override
            public Object getWidgetValue(View widgetValuableView) {
                return null;
            }

            @Override
            public void setWidgetValue(View widgetValuableView, Object value) {

            }
        });

        registerWidgetFactory("spinner", new WidgetFactory() {
            @Override
            public View getWidgetView(Context context, LayoutInflater layoutInflater, ViewGroup parentView, List<Object> data) {
                View spinnerLayout = layoutInflater.inflate(R.layout.base_form_spinner_label_leftright, parentView, false);
                return spinnerLayout;
            }

            @Override
            public int getViewIdForLabel() {
                return R.id.base_form_spinner_labelView;
            }

            @Override
            public void setLabel(View labelView, String label) {
                ((TextView)labelView).setText(label);
            }

            @Override
            public int getViewIdForSubLabel() {
                return 0;
            }

            @Override
            public void setSubLabel(View subLabelView, String subLabel) {

            }

            @Override
            public FormValidationUtil.AbstractValidator getValidator(Context context, View valuableView, View errorView, Object packet) {
                return new FormValidationUtil.EditTextValidator(context, valuableView, errorView);
            }

            @Override
            public int getViewIdForError() {
                return 0;
            }

            @Override
            public void setError(View errorView, String error) {

            }

            @Override
            public int getViewIdForValue() {
                return R.id.base_form_spinner_spinnerView;
            }

            @Override
            public Object getWidgetValue(View widgetValuableView) {
                int position = ((Spinner)widgetValuableView).getSelectedItemPosition();
                ArrayList<String> valueList = (ArrayList<String>) widgetValuableView.getTag();
                if(valueList == null){
                    return null;
                }
                return valueList.get(position);
            }

            @Override
            public void setWidgetValue(View widgetValuableView, Object value) {

            }
        });


        registerWidgetFactory("spinner2", new WidgetFactory() {
            @Override
            public View getWidgetView(Context context, LayoutInflater layoutInflater, ViewGroup parentView, List<Object> data) {
                View spinnerLayout = layoutInflater.inflate(R.layout.base_form_spinner_label_updown, parentView, false);
                return spinnerLayout;
            }

            @Override
            public int getViewIdForLabel() {
                return R.id.base_form_spinner_labelView;
            }

            @Override
            public void setLabel(View labelView, String label) {
                ((TextView)labelView).setText(label);
            }

            @Override
            public int getViewIdForSubLabel() {
                return 0;
            }

            @Override
            public void setSubLabel(View subLabelView, String subLabel) {

            }

            @Override
            public FormValidationUtil.AbstractValidator getValidator(Context context, View valuableView, View errorView, Object packet) {
                return new FormValidationUtil.EditTextValidator(context, valuableView, errorView);
            }

            @Override
            public int getViewIdForError() {
                return 0;
            }

            @Override
            public void setError(View errorView, String error) {

            }

            @Override
            public int getViewIdForValue() {
                return R.id.base_form_spinner_spinnerView;
            }

            @Override
            public Object getWidgetValue(View widgetValuableView) {
                int position = ((Spinner)widgetValuableView).getSelectedItemPosition();
                ArrayList<String> valueList = (ArrayList<String>) widgetValuableView.getTag();
                if(valueList == null){
                    return null;
                }
                return valueList.get(position);
            }

            @Override
            public void setWidgetValue(View widgetValuableView, Object value) {

            }
        });

        //Native Property Factory
        registerNativePropertyFactory("edittext", "inputtype", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.widget_edittext_edit);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setInputType");
                return nativeProperties;
            }
        });
        registerNativePropertyFactory("edittext2", "inputtype", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setInputType");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("date", "inputtype", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setInputType");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("date", "hintcolor", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_labelView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setTextColor");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("date", "textcolor", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setTextColor");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "hintcolor", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_labelView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setTextColor");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "textcolor", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setTextColor");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "backgroundcolor", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setBackgroundColor");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "counter", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setCounterEnabled");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "countermax", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setCounterMaxLength");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "focus", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setFocusable");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "click", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setClickable");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("edittext2", "enabled", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_edittext_edittextView);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setEnabled");
                return nativeProperties;
            }
        });

        registerNativePropertyFactory("button", "textcolor", new NativePropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_button);
                return widgetIds;
            }

            @Override
            public List<String> getNativePropertyMethod(String propertyKey) {
                List<String> nativeProperties = new ArrayList<>();
                nativeProperties.add("setTextColor");
                return nativeProperties;
            }
        });


        //Custom Property Factory
        registerCustomPropertyFactory("spinner", "setObjectData", new CustomPropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_spinner_spinnerView);
                return widgetIds;
            }

            @Override
            public Object runPropertyMethod(Context context, List<View> views, List<Object> arguments) {
                FormCommonUtil.setSpinnerList(context, (Spinner)views.get(0),(List<String>)arguments.get(0),(List<String>)arguments.get(1), null);
                return null;
            }
        });
        registerCustomPropertyFactory("spinner2", "setObjectData", new CustomPropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_spinner_spinnerView);
                return widgetIds;
            }

            @Override
            public Object runPropertyMethod(Context context, List<View> views, List<Object> arguments) {
                FormCommonUtil.setSpinnerList(context, (Spinner)views.get(0),(List<String>)arguments.get(0),(List<String>)arguments.get(1), null);
                return null;
            }
        });

        registerCustomPropertyFactory("edittext2", "setReadonly", new CustomPropertyFactory() {
            @Override
            public List<Integer> getViewIds() {
                List<Integer> widgetIds = new ArrayList<>();
                widgetIds.add(R.id.base_form_spinner_spinnerView);
                return widgetIds;
            }

            @Override
            public Object runPropertyMethod(Context context, List<View> views, List<Object> arguments) {
                views.get(0).setFocusable(false);
                views.get(0).setClickable(true);
                return null;
            }
        });


        //Argument Factory
        registerArgumentFactory("array", new ArgumentFactory() {
            @Override
            public Class getType(String propType) {
                return ArrayList.class;
            }

            @Override
            public Object getValue(Object propValue) {
                return propValue;
            }
        });
        registerArgumentFactory("locale", new ArgumentFactory() {
            @Override
            public Class getType(String propType) {
                return Locale.class;
            }

            @Override
            public Object getValue(Object propValue) {
                String[] propValueArray = ((String)propValue).split(",");
                return new Locale(propValueArray[0], propValueArray[1]);
            }
        });

        registerArgumentFactory("timezone", new ArgumentFactory() {
            @Override
            public Class getType(String propType) {
                return TimeZone.class;
            }

            @Override
            public Object getValue(Object propValue) {
                return TimeZone.getTimeZone((String)propValue);
            }
        });


        //validationRuleFactory
        registerValidationRuleFactory("fixcount", new ValidationRuleFactory() {
            @Override
            public FormValidationUtil.ValidatorRuleInterface getValidationRule(Context context, FormValidationUtil.AbstractValidator validator, List<Class> typeList, List<Object> valueList) {
                return new FormValidationUtil.CountValidatorRule((int) valueList.get(0));
            }
        });

        registerValidationRuleFactory("notempty", new ValidationRuleFactory() {
            @Override
            public FormValidationUtil.ValidatorRuleInterface getValidationRule(Context context, FormValidationUtil.AbstractValidator validator, List<Class> typeList, List<Object> valueList) {
                return new FormValidationUtil.NotEmptyValidatorRule();
            }
        });

        registerValidationRuleFactory("email", new ValidationRuleFactory() {
            @Override
            public FormValidationUtil.ValidatorRuleInterface getValidationRule(Context context, FormValidationUtil.AbstractValidator validator, List<Class> typeList, List<Object> valueList) {
                return new FormValidationUtil.EmailValidatorRule();
            }
        });
        registerValidationRuleFactory("url", new ValidationRuleFactory() {
            @Override
            public FormValidationUtil.ValidatorRuleInterface getValidationRule(Context context, FormValidationUtil.AbstractValidator validator, List<Class> typeList, List<Object> valueList) {
                return new FormValidationUtil.URLValidatorRule();
            }
        });
        registerValidationRuleFactory("date", new ValidationRuleFactory() {
            @Override
            public FormValidationUtil.ValidatorRuleInterface getValidationRule(Context context, FormValidationUtil.AbstractValidator validator, List<Class> typeList, List<Object> valueList) {
                return new FormValidationUtil.DateValidatorRule((String)valueList.get(0), (TimeZone) valueList.get(1), (Locale)valueList.get(2));
            }
        });
    }

}
