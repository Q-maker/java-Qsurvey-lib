package com.qmaker.survey.core.entities;

import com.google.gson.Gson;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import istat.android.base.tools.TextUtils;

public class Form {

    final HashMap<String, Field> fieldMap = new HashMap();

    public Form() {
        this(null);
    }

    public Form(HashMap<String, Field> fieldMap) {
        if (fieldMap != null) {
            this.fieldMap.putAll(fieldMap);
        }
    }

    public void clear() {
        fieldMap.clear();
    }

    public Field removeField(String fieldName) {
        return fieldMap.remove(fieldName);
    }

    public Field getField(String name) {
        return this.fieldMap.get(name);
    }

    public List<Field> getFields() {
        return new ArrayList(this.fieldMap.values());
    }

    public HashMap<String, Object> getNameValue() {
        HashMap<String, Object> result = new HashMap();
        for (Field field : getFields()) {
            result.put(field.getName(), field.getValue());
        }
        return result;
    }

    public boolean isFieldDefined(String name) {
        return this.fieldMap.containsKey(name);
    }

    public boolean hasMandatoryField() {
        List<Field> fields = getFields();
        for (Field field : fields) {
            if (field.isMandatory()) {
                return true;
            }
        }
        return false;
    }

    public Field putField(String name, Object value) {
        return putField(name, null, null, value);
    }

    public Field putField(String name, String inputType, String pattern, Object value) {
        FieldDefinition field = new FieldDefinition(name);
        field.setInputType(inputType);
        field.setPattern(pattern);
        if (field.setValue(value)) {
            return field;
        } else {
            return null;
        }
    }

    public Field put(Field field) {
        if (field != null) {
            return this.fieldMap.put(field.getName(), field);
        }
        return null;
    }

    public Field put(FieldDefinition field) {
        if (field != null) {
            return this.fieldMap.put(field.getName(), field);
        }
        return null;
    }

    public <T> HashMap<String, Field> putFields(HashMap<String, T> identity) {
        HashMap<String, Field> result = new HashMap();
        for (Map.Entry<String, T> entry : identity.entrySet()) {
            result.put(entry.getKey(), putField(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    public static class FieldDefinition extends Field {

        public FieldDefinition(String name) {
            super(name);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        public void setMasked(boolean masked) {
            this.masked = masked;
        }

        public void setInputType(String inputType) {
            this.inputType = inputType;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    public static class Field {
        String name;
        String inputType, pattern;
        Object value;
        boolean mandatory, masked;

        public Field(String name) {
            this.name = name;
        }

        Field() {

        }

        public boolean isMandatory() {
            return mandatory;
        }

        public boolean isMasked() {
            return masked;
        }

        public boolean setValue(Object value) {
            if (value != null && value instanceof CharSequence) {
                String charSequence = value.toString();
                if (!TextUtils.isEmpty(pattern) && !charSequence.matches(pattern)) {
                    return false;
                }
            }
            this.value = value;
            return true;
        }

        public String getName() {
            return name;
        }

        public String getInputType() {
            return inputType;
        }

        public String getPattern() {
            return pattern;
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
}
