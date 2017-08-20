package com.jcarsalade.realmbrowser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;

class DataRepository {
    private static final String REALM_ID_UNUSED_CHARS_REGEX = "((?i).realm$|[$._-])";

    private Map<Realm, Set<Class<? extends RealmModel>>> map;

    DataRepository(Map<Realm, Set<Class<? extends RealmModel>>> map) {
        this.map = map;
    }

    List<RealmJson> getRealms() {
        List<RealmJson> realms = new ArrayList<>();

        for (Realm r : map.keySet()) {
            String id = r.getConfiguration().getRealmFileName().replaceAll(REALM_ID_UNUSED_CHARS_REGEX, "");

            RealmJson realmJson = new RealmJson();
            realmJson.setId(id);
            realmJson.setFileName(r.getConfiguration().getRealmFileName());

            List<String> realmJsonClasses = new ArrayList<>();

            for (Class<? extends RealmModel> c : map.get(r)) {
                realmJsonClasses.add(c.getSimpleName());
            }

            realmJson.setClasses(realmJsonClasses);
            realms.add(realmJson);
        }

        return realms;
    }

    RealmJson getRealm(String realmId) {
        RealmJson realm = null;

        List<RealmJson> realms = getRealms();

        for (RealmJson r : realms) {
            if (r.getId().equals(realmId)) {
                realm = r;
            }
        }

        return realm;
    }

    List<FieldJson> getSchema(String realmId, String className) {
        List<FieldJson> schema = new ArrayList<>();

        Map.Entry<Realm, Class> entry = getRealmAndClassFromParams(realmId, className);

        if (entry == null) {
            return null;
        }

        Realm selectedRealm = entry.getKey();
        Class selectedClass = entry.getValue();

        RealmObjectSchema realmObjectSchema = selectedRealm.getSchema().get(selectedClass.getSimpleName());
        Set<String> fieldNames = realmObjectSchema.getFieldNames();

        for (String f : fieldNames) {
            FieldJson fieldJson = new FieldJson();

            fieldJson.setFieldName(f);
            fieldJson.setPrimaryKey(realmObjectSchema.isPrimaryKey(f));
            fieldJson.setIndex(realmObjectSchema.hasIndex(f));
            fieldJson.setRequired(realmObjectSchema.isRequired(f));
            fieldJson.setNullable(realmObjectSchema.isNullable(f));

            String fieldType = realmObjectSchema.getFieldType(f).toString();

            if (fieldType.equalsIgnoreCase("LIST")) {
                String longName;

                try {
                    longName = selectedClass.getDeclaredField(f).getGenericType().toString();
                } catch (NoSuchFieldException e) {
                    continue;
                }

                fieldType = longName.substring(
                        longName.lastIndexOf(".") + 1,
                        longName.length() - 1);

                fieldJson.setRealmList(true);
            }

            fieldJson.setFieldType(fieldType);
            schema.add(fieldJson);
        }

        return schema;
    }

    List<Map<String, Object>> getData(String realmId, String className) {
        List<Map<String, Object>> data = new ArrayList<>();

        List<FieldJson> schema = getSchema(realmId, className);
        Map.Entry<Realm, Class> entry = getRealmAndClassFromParams(realmId, className);

        if (entry == null) {
            return null;
        }

        Realm selectedRealm = entry.getKey();
        Class selectedClass = entry.getValue();

        RealmResults realmResults = selectedRealm.where(selectedClass).findAll();

        for (Object o : realmResults) {
            Map<String, Object> objectMap = new HashMap<>();
            DynamicRealmObject dynamicRealmObject = new DynamicRealmObject((RealmModel) o);

            for (FieldJson fieldJson : schema) {
                if (fieldJson.isRealmList()) {
                    continue;
                }

                String fieldName = fieldJson.getFieldName();
                objectMap.put(fieldName, dynamicRealmObject.get(fieldName));
            }

            data.add(objectMap);
        }

        return data;
    }

    private Map.Entry<Realm, Class> getRealmAndClassFromParams(String realmId, String className) {
        Realm selectedRealm = null;
        Class selectedClass = null;

        for (Realm r : map.keySet()) {
            String id = r.getConfiguration().getRealmFileName().replaceAll(REALM_ID_UNUSED_CHARS_REGEX, "");

            if (id.equals(realmId)) {
                selectedRealm = Realm.getInstance(r.getConfiguration());

                for (Class c : map.get(r)) {
                    if (c.getSimpleName().equals(className)) {
                        selectedClass = c;
                        break;
                    }
                }

                break;
            }
        }

        if (selectedRealm == null || selectedClass == null || !selectedRealm.getSchema().contains(className)) {
            return null;
        }

        return new AbstractMap.SimpleEntry<>(selectedRealm, selectedClass);
    }
}
