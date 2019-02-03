package com.qmaker.survey.core.entities;

import com.google.gson.Gson;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import istat.android.base.tools.TextUtils;
import istat.android.base.utils.ListLinkedHashMap;

public class Form {
    boolean allowDynamicFields = true;
    final HashMap<String, Field> fieldMap = new HashMap();

    public Form() {
        this(null);
    }

    public <T extends Field> Form(HashMap<String, T> fieldMap) {
        if (fieldMap != null) {
            this.fieldMap.putAll(fieldMap);
        }
    }

    public boolean isAllowDynamicFields() {
        return allowDynamicFields;
    }

    public void clear() {
        fieldMap.clear();
    }

    public Field remove(String fieldName) {
        return fieldMap.remove(fieldName);
    }

    public Field getField(String name) {
        return this.fieldMap.get(name);
    }

    public List<Field> getFields() {
        List<Field> fields = new ArrayList(this.fieldMap.values());
        return fields;
    }

    public HashMap<String, Object> getContentMap() {
        HashMap<String, Object> result = new HashMap();
        for (Field field : getFields()) {
            result.put(field.getName(), field.getValue());
        }
        return result;
    }

    public boolean isDefined(String name) {
        return this.fieldMap.containsKey(name);
    }

    public boolean isEmpty(String name) {
        return !TextUtils.isEmpty(fieldMap.get(name));
    }

    public Field put(String name, Object value) throws IllegalArgumentException {
        Field field = getField(name);
        if (field == null && allowDynamicFields) {
            field = new Field(name);
            fieldMap.put(name, field);
        }
        if (field == null) {
            throw new IllegalArgumentException("Field with name='" + name + "' is not defined as current allowed Form field.");
        }
        field.setValue(value);
        return field;
    }


    public <T> HashMap<String, Field> putAll(HashMap<String, T> nameValue) throws IllegalArgumentException {
        HashMap<String, Field> result = new HashMap();
        for (Map.Entry<String, T> entry : nameValue.entrySet()) {
            result.put(entry.getKey(), put(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    public static class Definition {
        boolean allowDynamicFields = true;
        final HashMap<String, FieldDefinition> fieldMap = new HashMap();

        public void setAllowDynamicFields(boolean allowDynamicFields) {
            this.allowDynamicFields = allowDynamicFields;
        }

        public FieldDefinition put(String name, Object value) throws PatternMatchError {
            return put(name, Field.INPUT_TYPE_TEXT, value);
        }

        public FieldDefinition put(String name, String inputType) throws PatternMatchError {
            return put(name, inputType, "");
        }

        public FieldDefinition put(String fieldName, String inputType, Object value) throws PatternMatchError {
            FieldDefinition field = fieldMap.get(fieldName);
            if (field == null) {
                field = new FieldDefinition(fieldName);
                fieldMap.put(fieldName, field);
            }
            field.setValue(value);
            field.setInputType(inputType);
            for (Map.Entry<String, String> entry : field.patternErrorMessageMap.entrySet()) {
                if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(value) && !value.toString().matches(entry.getKey())) {
                    throw new PatternMatchError(field, entry.getKey(), entry.getValue());
                }
            }
            fieldMap.put(fieldName, field);
            return field;
        }

        public FieldDefinition putFieldValidator(String fieldName, String pattern, String validationErrorMessage) throws PatternMatchError {
            FieldDefinition field = fieldMap.get(fieldName);
            if (field == null) {
                field = new FieldDefinition(fieldName);
                fieldMap.put(fieldName, field);
            }
            field.putValidator(pattern, validationErrorMessage);
            if (!TextUtils.isEmpty(pattern) && !TextUtils.isEmpty(field.getValue()) && !field.getValue().toString().matches(pattern)) {
                throw new PatternMatchError(field, pattern, validationErrorMessage);
            }
            fieldMap.put(fieldName, field);
            return field;
        }

        public FieldDefinition put(Field field) {
            if (field != null) {
                return this.fieldMap.put(field.getName(), new FieldDefinition(field));
            }
            return null;
        }

        public FieldDefinition put(FieldDefinition field) {
            if (field != null) {
                return this.fieldMap.put(field.getName(), field);
            }
            return null;
        }

        public Form create() {
            Form form = new Form(fieldMap);
            form.allowDynamicFields = allowDynamicFields;
            return form;
        }

        public void clear() {
            fieldMap.clear();
        }

        public <T> HashMap<String, Field> putAll(HashMap<String, T> nameValue) throws IllegalArgumentException {
            HashMap<String, Field> result = new HashMap();
            for (Map.Entry<String, T> entry : nameValue.entrySet()) {
                result.put(entry.getKey(), put(entry.getKey(), entry.getValue()));
            }
            return result;
        }
    }

    public static class FieldDefinition extends Field {

        public FieldDefinition() {

        }

        public FieldDefinition(Field field) {
            this.name = field.name;
            this.masked = field.masked;
            this.inputType = field.inputType;
            this.value = field.value;
        }

        public FieldDefinition(String name) {
            super(name);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setMasked(boolean masked) {
            this.masked = masked;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public Field putValidator(String pattern, String errorMessage) {
            patternErrorMessageMap.put(pattern, errorMessage);
            return this;
        }
    }

    public static class Field {
        public final static String
                INPUT_TYPE_TEXT = "text",
                INPUT_TYPE_NUMBER = "number",
                INPUT_TYPE_PHONE = "phone",
                INPUT_TYPE_DATE = "date";
        String name;
        String inputType;
        Object value;
        boolean masked;
        protected final Map<String, String> patternErrorMessageMap = new LinkedHashMap<>();

        public Field(String name) {
            this.name = name;
        }

        Field() {

        }


        public List<PatternMatchError> checkup() {
            List<PatternMatchError> out = new ArrayList<>();
            for (Map.Entry<String, String> entry : patternErrorMessageMap.entrySet()) {
                if (!getValueString("").matches(entry.getKey())) {
                    out.add(new PatternMatchError(this, entry.getKey(), entry.getValue()));
                }
            }
            return out;
        }

        public boolean isMasked() {
            return masked;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getInputType() {
            return inputType;
        }


        public <T> T getValue() {
            return (T) value;
        }

        public String getValueString() {
            return getValueString(null);
        }

        public String getValueString(String defaultValue) {
            try {
                return value.toString();
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public int getValueInt() {
            return getValueInt(0);
        }

        public int getValueInt(int defaultValue) {
            try {
                return (int) getValueDouble(defaultValue);
            } catch (Exception e) {
                e.printStackTrace();
                return defaultValue;
            }
        }

        public long getValueLong() {
            return getValueLong(0);
        }

        public long getValueLong(long defaultValue) {
            try {
                return (long) getValueDouble(defaultValue);
            } catch (Exception e) {
                e.printStackTrace();
                return defaultValue;
            }
        }

        public double getValueDouble() {
            return getValueDouble(0);
        }

        public double getValueDouble(double defaultValue) {
            try {
                return Double.parseDouble(getValue().toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public float getValueFloat(String key) {
            return getValueFloat(key, 0);
        }

        public float getValueFloat(String key, float defaultValue) {
            try {
                return (float) getValueDouble(defaultValue);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public <T> T getValue(Class<T> cLass) {
            return getValue(cLass, null);
        }

        public <T> T getValue(Class<T> cLass, T defaultValue) {
            try {
                Object value = getValue();
                if (value == null) {
                    return null;
                }
                if (value.getClass().isAssignableFrom(cLass)) {
                    return (T) value;
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(value);
                    return gson.fromJson(json, cLass);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return defaultValue;
            }
        }

        public <T> T getValue(Type type) {
            return getValue(type, null);
        }

        public <T> T getValue(Type type, T defaultValue) {
            try {
                Object value = getValue();
                if (value == null) {
                    return null;
                }
//            if (value.getValueClass().isAssignableFrom(cLass)) {
//                return (T) value;
//            } else {
                Gson gson = new Gson();
                String json = gson.toJson(value);
                return gson.fromJson(json, type);
//            }
            } catch (Exception e) {
                e.printStackTrace();
                return defaultValue;
            }
        }

        public boolean getValueBoolean() {
            return getValueBoolean(false);
        }

        public boolean getValueBoolean(boolean deFault) {
            try {
                return Boolean.parseBoolean(getValue().toString());
            } catch (Exception e) {
                return deFault;
            }
        }
    }

    public void checkUp() throws Error {
        List<Field> fields = getFields();
        ListLinkedHashMap<Field, PatternMatchError> errorMap = new ListLinkedHashMap<>();
        List<PatternMatchError> errors;
        for (Field field : fields) {
            errors = field.checkup();
            if (errors != null && !errors.isEmpty()) {
                errorMap.put(field, errors);
            }
        }
        if (!errorMap.isEmpty()) {
            throw new Error(errorMap);
        }
    }

    public static class PatternMatchError extends IllegalArgumentException {
        Field field;
        String pattern;

        public PatternMatchError(Field field, String pattern, String message) {
            super(message);
            this.pattern = pattern;
            this.field = field;
        }

        public Field getField() {
            return field;
        }

        public String getPattern() {
            return pattern;
        }
    }

    public static class Error extends Exception {
        ListLinkedHashMap<Field, PatternMatchError> fieldErrorMap = new ListLinkedHashMap<>();
        HashMap<String, Field> nameFieldMap = new HashMap<>();

        public Error(List<PatternMatchError> errors) {
            for (PatternMatchError patternMatchError : errors) {
                fieldErrorMap.append(patternMatchError.getField(), patternMatchError);
            }
        }

        public Error(ListLinkedHashMap<Field, PatternMatchError> errorMap) {
            this.fieldErrorMap = errorMap;
        }

        public List<Field> getErrorFields() {
            return new ArrayList<>(fieldErrorMap.keySet());
        }

        public List<PatternMatchError> getFieldErrors(String fieldName) {
            Field field = nameFieldMap.get(fieldName);
            if (field != null) {
                return getFieldErrors(field);
            }
            List<PatternMatchError> out = new ArrayList<>();
            for (Map.Entry<Field, List<PatternMatchError>> entry : fieldErrorMap.entrySet()) {
                nameFieldMap.put(entry.getKey().getName(), entry.getKey());
                if (field.getName().equals(fieldName)) {
                    out = entry.getValue();
                }
            }
            return out;
        }

        public List<PatternMatchError> getFieldErrors(Field field) {
            return fieldErrorMap.get(field);
        }

    }
}
