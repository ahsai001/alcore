package com.ahsailabs.alutils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ahsai on 11/21/2017.
 */

public class FormValidationUtil {
    public static final String MESSAGE_PLEASE_FILL_THIS = "Please fill this";
    public static final String MESSAGE_EMAIL_IS_INVALID = "Email is invalid";

    public static final String MESSAGE_PLEASE_INSERT_WITH_VALID_FORMAT = "Please insert with valid format";
    public static final String MESSAGE_NEED_SET_REGEX_PATTERN = "You need to set Regexp Pattern first";
    public static final String MESSAGE_PLEASE_FILL_ALPHA_NUMERIC_ONLY = "Please fill with alpha numeric only";
    public static final String MESSAGE_PLEASE_FILL_NUMERIC_ONLY = "Please fill with numeric only";
    public static final String MESSAGE_VALUE_IS_DIFFERENT = "The value is different with %1$s";
    public static final String MESSAGE_DATA_MUST_BE_COUNT_DIGITS = "Data must be %1$d %2$s";
    public static final String MESSAGE_PHONE_FORMAT_IS_INVALID = "Phone format is invalid";
    public static final String MESSAGE_YOU_MUST_CHECK_THIS = "You must check this";
    public static final String MESSAGE_URL_FORMAT_IS_INVALID = "Url format is invalid";
    public static final String MESSAGE_DATE_FORMAT_IS_INVALID = "Date format is invalid";

    public static final int DefaultType = 1;
    public static final int IdleType = 2;
    public static final int UnfocusType = 3;
    private Context context;
    private int defaultValidationType;
    private List<AbstractValidator> mValidatorList;
    private ViewEnablerUtil viewEnablerUtil = null;

    public FormValidationUtil(Context context){
        this(context, DefaultType);
    }

    public FormValidationUtil(Context context, int defaultValidationType){
        this.context = context.getApplicationContext();
        mValidatorList = new ArrayList<>();
        this.defaultValidationType = defaultValidationType;
    }


    public void addValidator(AbstractValidator validator){
        mValidatorList.add(validator);
    }

    public void setEnablerView(View view){
        viewEnablerUtil = new ViewEnablerUtil(view, mValidatorList.size());
        viewEnablerUtil.init();
    }

    public void init(){
        for (AbstractValidator validator : mValidatorList){
            if(viewEnablerUtil != null) {
                validator.addOnValidationCallback(new OnValidationCallback() {
                    @Override
                    public boolean onSuccess(View view, ValidatorRuleInterface validatorRule) {
                        return false;
                    }

                    @Override
                    public boolean onFailed(View view, ValidatorRuleInterface validatorRule, String message) {
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
                });
                validator.setAlwaysShowErrorOnView(true);
                validator.setValidationType(FormValidationUtil.IdleType);
            }

            int validationType = validator.getValidationType();

            if(validationType == IdleType){
                enableTypeIdleValidation(validator);
            } else if(validationType == UnfocusType){
                enableUnfocusValidation(validator);
            }
        }
    }

    private void enableUnfocusValidation(final AbstractValidator validator) {
        CommonUtil.performTaskWhenUnFocus(validator.mView, new Runnable() {
            @Override
            public void run() {
                validator.validate();
            }
        });
    }

    private void enableTypeIdleValidation(final AbstractValidator validator){
        if(validator.mView instanceof EditText) {
            CommonUtil.performTaskWhenTypeIdle((EditText) validator.mView, new Runnable() {
                @Override
                public void run() {
                    validator.validate();
                }
            });
        }
    }

    public AbstractValidator getValidator(int position) {
        if(position < mValidatorList.size()) {
            return mValidatorList.get(position);
        }
        return null;
    }

    public int getValidatorCount(){
        return mValidatorList.size();
    }

    public void removeValidator(AbstractValidator validator){
        mValidatorList.remove(validator);
    }

    public boolean validate() throws ValidatorException {
        boolean isFirsErrorFocused = false;
        for (int x=0; x<mValidatorList.size(); x++){
            AbstractValidator validator = mValidatorList.get(x);
            for (int y=0; y<validator.mRuleList.size(); y++){
                ValidatorRuleInterface rule = validator.mRuleList.get(y);
                boolean isValid = rule.isValid(validator.mView, validator.packet);
                if (!isValid) {
                    validator.showError(rule.getMessage());
                    if (!isFirsErrorFocused) {
                        validator.mView.requestFocus();
                        isFirsErrorFocused = true;
                    }
                    break;
                } else {
                    validator.showError(null);
                }
            }
        }

        return !isFirsErrorFocused;
    }

    public boolean validate(OnValidationCallback onValidationCallback) throws ValidatorException {
        int totalRule = 0;
        int totalSuccessRule = 0;
        for (int x=0; x<mValidatorList.size(); x++){
            AbstractValidator validator = mValidatorList.get(x);
            totalRule += validator.mRuleList.size();
            for (int y=0; y<validator.mRuleList.size(); y++){
                ValidatorRuleInterface rule = validator.mRuleList.get(y);
                boolean isValid = rule.isValid(validator.mView, validator.packet);
                if (!isValid) {
                    if(onValidationCallback.onFailed(validator.mView, rule, rule.getMessage()))break;
                } else {
                    totalSuccessRule++;
                    if(onValidationCallback.onSuccess(validator.mView, rule))break;
                }
            }
        }
        onValidationCallback.onComplete(null, totalRule == totalSuccessRule);
        return totalRule == totalSuccessRule;
    }

    public static class EditTextValidator extends AbstractValidator{
        public EditTextValidator(Context context, View mView, View mErrorView, Object packet) {
            super(context, mView, mErrorView, packet);
        }

        public EditTextValidator(Context context, View mView, View mErrorView) {
            super(context, mView, mErrorView);
        }

        public EditTextValidator(Context context, View mView) {
            super(context, mView);
        }

        public EditTextValidator(Context context, int viewResourceId, int errorViewResoureId, Object packet) {
            super(context, viewResourceId, errorViewResoureId, packet);
        }

        public EditTextValidator(Context context, int viewResourceId, int errorViewResoureId) {
            super(context, viewResourceId, errorViewResoureId);
        }

        public EditTextValidator(Context context, int viewResourceId) {
            super(context, viewResourceId);
        }

        @Override
        public void showError(String message) {
            if(mErrorView != null) {
                ((TextInputLayout) mErrorView).setError(message);
            }
        }
    }

    public static class EditText2Validator extends AbstractValidator{


        public EditText2Validator(Context context, View mView, View mErrorView, Object packet) {
            super(context, mView, mErrorView, packet);
        }

        public EditText2Validator(Context context, View mView, View mErrorView) {
            super(context, mView, mErrorView);
        }

        public EditText2Validator(Context context, View mView) {
            super(context, mView);
        }

        public EditText2Validator(Context context, int viewResourceId, int errorViewResoureId, Object packet) {
            super(context, viewResourceId, errorViewResoureId, packet);
        }

        public EditText2Validator(Context context, int viewResourceId, int errorViewResoureId) {
            super(context, viewResourceId, errorViewResoureId);
        }

        public EditText2Validator(Context context, int viewResourceId) {
            super(context, viewResourceId);
        }

        @Override
        public void showError(String message) {
            ((EditText)mErrorView).setError(message);
        }
    }

    //Validator class
    public static abstract class AbstractValidator{
        protected Context mContext;
        protected View mView;
        protected View mErrorView;
        protected Object packet;
        protected List<ValidatorRuleInterface> mRuleList;
        protected List<OnValidationCallback> onValidationCallbackList = new ArrayList<>();
        protected boolean alwaysShowErrorOnView = false;
        protected int validationType = DefaultType;

        public AbstractValidator(Context context, View mView, View mErrorView, Object packet){
            setup(context,mView,mErrorView,packet);
        }

        public AbstractValidator(Context context, View mView, View mErrorView){
            this(context,mView, mErrorView, null);
        }

        public AbstractValidator(Context context, View mView){
            this(context,mView, null);
        }

        public AbstractValidator(Context context, int viewResourceId, int errorViewResoureId, Object packet){
            View editText = ((Activity)context).findViewById(viewResourceId);
            View errorText = ((Activity)context).findViewById(errorViewResoureId);
            setup(context,editText,errorText,packet);
        }

        public AbstractValidator(Context context, int viewResourceId, int errorViewResoureId){
            this(context, viewResourceId, errorViewResoureId, null);
        }

        public AbstractValidator(Context context, int viewResourceId){
            this(context,viewResourceId,viewResourceId);
        }

        protected void setup(Context context, View mView, View mErrorView, Object packet){
            this.mContext = context.getApplicationContext();
            this.mView = mView;
            this.mErrorView = mErrorView;
            this.packet = packet;
            mRuleList = new ArrayList<>();
        }

        public ValidatorRuleInterface getRule(int position){
            return mRuleList.get(position);
        }

        public int getRuleCount(){
            return mRuleList.size();
        }

        public void setPacket(Object packet) {
            this.packet = packet;
        }

        public AbstractValidator addRule(ValidatorRuleInterface validatorRule){
            mRuleList.add(validatorRule);
            return this;
        }

        public AbstractValidator addOnValidationCallback(OnValidationCallback onValidationCallback) {
            this.onValidationCallbackList.add(onValidationCallback);
            return this;
        }

        public AbstractValidator setAlwaysShowErrorOnView(boolean alwaysShowErrorOnView) {
            this.alwaysShowErrorOnView = alwaysShowErrorOnView;
            return this;
        }

        public int getValidationType() {
            return validationType;
        }

        public AbstractValidator setValidationType(int validationType) {
            this.validationType = validationType;
            return this;
        }

        public void validate(){
            int successCount = 0;
            for (int y=0; y<mRuleList.size(); y++){
                ValidatorRuleInterface rule = mRuleList.get(y);
                boolean isValid = false;
                try {
                    isValid = rule.isValid(mView, packet);
                } catch (ValidatorException e) {
                    e.printStackTrace();
                }
                if (!isValid) {
                    if(onValidationCallbackList.size() > 0){
                        if(alwaysShowErrorOnView){
                           showError(rule.getMessage());
                        }
                        for (OnValidationCallback callback : onValidationCallbackList){
                            if(callback.onFailed(mView, rule, rule.getMessage()))break;
                            //TODO: need riset in break statement
                        }

                    } else {
                        showError(rule.getMessage());
                    }
                } else {
                    if(onValidationCallbackList.size() > 0){
                        successCount++;
                        if(alwaysShowErrorOnView){
                            showError(null);
                        }
                        for (OnValidationCallback callback : onValidationCallbackList){
                            if(callback.onSuccess(mView, rule))break;
                            //TODO: need riset in break statement
                        }
                    } else {
                        showError(null);
                    }
                }
            }
            if(onValidationCallbackList.size() > 0){
                for (OnValidationCallback callback : onValidationCallbackList) {
                    callback.onComplete(mView, successCount == mRuleList.size());
                }
            }
        }

        public abstract void showError(String message);
    }

    public interface OnValidationCallback{
        boolean onSuccess(View view, ValidatorRuleInterface validatorRule);
        boolean onFailed(View view, ValidatorRuleInterface validatorRule, String message);
        void onComplete(View view, boolean allRuleValid);
    }


    public interface ValidatorRuleInterface {
        boolean isValid(View view, Object packet) throws ValidatorException;
        String getMessage();
        void setErrorMessage(String mErrorMessage);
    }

    public static abstract class EditTextValidatorRule implements ValidatorRuleInterface{
        private String mErrorMessage;

        public abstract boolean isValid(TextView textView, String value, Object packet) throws ValidatorException;

        public EditTextValidatorRule(){
        }
        public EditTextValidatorRule(String errorMessage){
            this.mErrorMessage = errorMessage;
        }

        @Override
        public boolean isValid(View view, Object packet) throws ValidatorException{
            TextView textView = (TextView)view;
            return isValid(textView,textView.getText().toString(), packet);
        }

        @Override
        public String getMessage() {
            return mErrorMessage;
        }

        @Override
        public void setErrorMessage(String mErrorMessage) {
            this.mErrorMessage = mErrorMessage;
        }
    }

    public static abstract class CheckBoxValidatorRule implements ValidatorRuleInterface{
        private String mErrorMessage;

        public abstract boolean isValid(CheckBox checkBox, boolean isChecked, Object packet) throws ValidatorException;

        public CheckBoxValidatorRule(){
        }

        public CheckBoxValidatorRule(String errorMessage){
            this.mErrorMessage = errorMessage;
        }

        @Override
        public boolean isValid(View view, Object packet) throws ValidatorException{
            CheckBox checkBox = (CheckBox)view;
            return isValid(checkBox, checkBox.isChecked(), packet);
        }

        @Override
        public String getMessage() {
            return mErrorMessage;
        }

        @Override
        public void setErrorMessage(String mErrorMessage) {
            this.mErrorMessage = mErrorMessage;
        }
    }

    //Validation Rules definition
    public static class NotEmptyValidatorRule extends EditTextValidatorRule{
        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            return !TextUtils.isEmpty(value);
        }

        public NotEmptyValidatorRule() {
            setErrorMessage(MESSAGE_PLEASE_FILL_THIS);
        }

        public NotEmptyValidatorRule(String errorMessage) {
            setErrorMessage(errorMessage);
        }
    }

    public static class EmailValidatorRule extends EditTextValidatorRule{
        private String mDomainName = "";

        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            if (!TextUtils.isEmpty(value)) {
                if (TextUtils.isEmpty(mDomainName)) {
                    Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");
                    Matcher matcher = pattern.matcher(value);
                    return matcher.matches();
                } else {
                    Pattern pattern = Pattern.compile(".+@" + mDomainName);
                    Matcher matcher = pattern.matcher(value);
                    return matcher.matches();
                }
            }else{
                return true;
            }
        }

        public EmailValidatorRule() {
            setErrorMessage(MESSAGE_EMAIL_IS_INVALID);
        }

        public EmailValidatorRule(String errorMessage) {
            super(errorMessage);
        }

        public void setDomainName(String domainName) {
            mDomainName = domainName;
        }
    }


    public static class RegExpValidatorRule extends EditTextValidatorRule{
        private Pattern mPattern;

        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            if (mPattern != null) {
                return mPattern.matcher(value).matches();
            }
            throw new ValidatorException(MESSAGE_NEED_SET_REGEX_PATTERN);
        }

        public RegExpValidatorRule() {
            setErrorMessage(MESSAGE_PLEASE_INSERT_WITH_VALID_FORMAT);
        }

        public RegExpValidatorRule(String errorMessage) {
            super(errorMessage);
        }

        public void setPattern(String pattern) {
            mPattern = Pattern.compile(pattern);
        }

        public void setPattern(Pattern pattern) {
            mPattern = pattern;
        }
    }


    public static class AlphaNumericValidatorRule extends EditTextValidatorRule{
        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            if(TextUtils.isEmpty(value))return true;
            return TextUtils.isDigitsOnly(value);
        }

        public AlphaNumericValidatorRule() {
            setErrorMessage(MESSAGE_PLEASE_FILL_ALPHA_NUMERIC_ONLY);
        }

        public AlphaNumericValidatorRule(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class NumericValidatorRule extends EditTextValidatorRule{
        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            if(TextUtils.isEmpty(value))return true;
            if (value == null || value.length() == 0)
                return false;
            for (int i = 0; i < value.length(); i++) {
                if (!Character.isDigit(value.charAt(i)))
                    return false;
            }
            return true;
        }

        public NumericValidatorRule() {
            setErrorMessage(MESSAGE_PLEASE_FILL_NUMERIC_ONLY);
        }

        public NumericValidatorRule(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class SameValueValidatorRule extends EditTextValidatorRule{

        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            View comparedView = (View)packet;
            String comparedValue = ((TextView)comparedView).getText().toString();
            return value.equals(comparedValue);
        }

        public SameValueValidatorRule(String comparedFieldName) {
            setErrorMessage(String.format(MESSAGE_VALUE_IS_DIFFERENT,comparedFieldName));
        }

        public SameValueValidatorRule(String errorMessage, String comparedFieldName) {
            super(errorMessage);
        }


    }

    public static class CountValidatorRule extends EditTextValidatorRule{
        private int count;
        public CountValidatorRule(int count) {
            setErrorMessage(String.format(MESSAGE_DATA_MUST_BE_COUNT_DIGITS,count,(count>1?"digits":"digit")));
            this.count = count;
        }

        public CountValidatorRule(String errorMessage, int count) {
            super(errorMessage);
            this.count = count;
        }

        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            return value.length() == count;
        }
    }

    public static class PhoneValidatorRule extends EditTextValidatorRule{
        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            if(TextUtils.isEmpty(value))return true;
            return Patterns.PHONE.matcher(value).matches();
        }

        public PhoneValidatorRule() {
            setErrorMessage(MESSAGE_PHONE_FORMAT_IS_INVALID);
        }

        public PhoneValidatorRule(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class MustCheckedValidatorRule extends CheckBoxValidatorRule{
        public MustCheckedValidatorRule(Context context) {
            setErrorMessage(MESSAGE_YOU_MUST_CHECK_THIS);
        }

        @Override
        public boolean isValid(CheckBox checkBox, boolean isChecked, Object packet) throws ValidatorException {
            if(isChecked){
                return true;
            }else{
                //invalid state :
                //1. remove error message after checked
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            buttonView.setError(null);
                            buttonView.setFocusableInTouchMode(false);
                        }
                    }
                });
                checkBox.setFocusableInTouchMode(true);

            }
            return false;
        }

        public MustCheckedValidatorRule(String errorMessage) {
            super(errorMessage);
        }
    }


    public static class URLValidatorRule extends EditTextValidatorRule{
        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            if(TextUtils.isEmpty(value))return true;
            return Patterns.WEB_URL.matcher(value).matches();
        }

        public URLValidatorRule() {
            setErrorMessage(MESSAGE_URL_FORMAT_IS_INVALID);
        }

        public URLValidatorRule(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class DateValidatorRule extends EditTextValidatorRule{
        private String dateFormat;
        private TimeZone timeZone;
        private Locale locale;
        public DateValidatorRule(String dateFormat, TimeZone timeZone, Locale locale) {
            setErrorMessage(MESSAGE_DATE_FORMAT_IS_INVALID);
            this.dateFormat = dateFormat;
            this.timeZone = timeZone;
            this.locale = locale;
        }

        public DateValidatorRule(String errorMessage, String dateFormat, TimeZone timeZone, Locale locale) {
            super(errorMessage);
            this.dateFormat = dateFormat;
            this.timeZone = timeZone;
            this.locale = locale;
        }

        @Override
        public boolean isValid(TextView textView, String value, Object packet) throws ValidatorException {
            try {
                DateFormat df = new SimpleDateFormat(dateFormat, locale);
                df.setLenient(false);
                if(timeZone != null) {
                    df.setTimeZone(timeZone);
                }
                df.parse(value);
                return true;
            } catch (ParseException e) {
                throw new ValidatorException(e.getMessage());
            }
        }
    }



    public static class ValidatorException extends Exception {
        public ValidatorException() {
            super();
        }

        public ValidatorException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ValidatorException(String detailMessage) {
            super(detailMessage);
        }

        public ValidatorException(Throwable throwable) {
            super(throwable);
        }
    }



    //helper method
    public AbstractValidator addNotEmptyValidatorForView(View view){
       return addNotEmptyValidatorForView(view, null);
    }

    public AbstractValidator addNotEmptyValidatorForView(View view, String errorMessage){
        return addNotEmptyValidatorForView(defaultValidationType, view, errorMessage);
    }

    public AbstractValidator addNotEmptyValidatorForView(int validationType, View view){
        return addNotEmptyValidatorForView(validationType, view, null);
    }

    public AbstractValidator addNotEmptyValidatorForView(int validationType, View view, String errorMessage){
        AbstractValidator newValidator = new FormValidationUtil.EditTextValidator(context, view);
        newValidator.addRule(TextUtils.isEmpty(errorMessage)?new NotEmptyValidatorRule():new NotEmptyValidatorRule(errorMessage));
        newValidator.setValidationType(validationType);
        addValidator(newValidator);
        return newValidator;
    }

    public AbstractValidator addNewValidatorForView(View view){
        return addNewValidatorForView(defaultValidationType, view);
    }

    public AbstractValidator addNewValidatorForView(int validationType, View view){
        AbstractValidator newValidator = new FormValidationUtil.EditTextValidator(context, view);
        newValidator.setValidationType(validationType);
        addValidator(newValidator);
        return newValidator;
    }
}
