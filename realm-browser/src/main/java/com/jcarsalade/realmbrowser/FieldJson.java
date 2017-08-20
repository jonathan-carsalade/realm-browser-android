package com.jcarsalade.realmbrowser;

class FieldJson {
    private String fieldName;
    private String fieldType;
    private boolean primaryKey;
    private boolean index;
    private boolean required;
    private boolean nullable;
    private boolean realmList;

    public FieldJson() {

    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isRealmList() {
        return realmList;
    }

    public void setRealmList(boolean realmList) {
        this.realmList = realmList;
    }
}
