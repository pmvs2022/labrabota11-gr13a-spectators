package com.spectator.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spectator.data.JsonObjectConvertable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class JsonIO {

    private File file;
    private JSONObject jsonFile;
    private String mainArrayKey;
    private MODE mode;
    private boolean isInit = false;

    public JsonIO(File dir, String path, String mainArrayKey, boolean isInitNow) {
        this.file = new File(dir, path);
        this.mainArrayKey = mainArrayKey;
        this.mode = MODE.READ_WRITE;
        if (isInitNow) {
            init();
        }
    }

    public JsonIO(File dir, String path, String mainArrayKey, MODE mode, boolean isInitNow) {
        this.file = new File(dir, path);
        this.mainArrayKey = mainArrayKey;
        this.mode = mode;
        if (isInitNow) {
            init();
        }
    }

    //temp method
    private void retrospectivelyRewrite(ArrayList<? extends JsonObjectConvertable> newList) {
        jsonFile = createJSONObject(mainArrayKey);
        for (int i = 0; i < newList.size(); i++) {
            addObjectToJSON(newList.get(i).toJSONObject(), mainArrayKey);
        }
        writeToFile(jsonFile);

    }

    public void init() {
        if (mode == MODE.WRITE_ONLY_EOF) {
            checkFileExistence();
        }
        else {
            read();
        }
        isInit = true;
    }

    public void update() {
        init();
    }

    private void checkFileExistence() {
        if (!file.exists()) {
            writeToFile(createJSONObject(mainArrayKey));
        }
    }

    private JSONObject read() {
        checkFileExistence();
        try {
            jsonFile = new JSONObject(readJSONFromFile());
            return jsonFile;
        } catch (JSONException e) {
            //Potential data loss
            e.printStackTrace();
            Log.e("JsonIO error", "Json file read error, recreating json.");
            writeToFile(createJSONObject(mainArrayKey));
            return read();
        }
    }

    //Creating Object for JSON file
    private JSONObject createJSONObject(String arrayKey) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonObject.put(arrayKey, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    //Adding a new object to JSON file
    private void addObjectToJSON(@NonNull JSONObject object, String arrayKey) {
        if (arrayKey == null)
            arrayKey = mainArrayKey;
        if (jsonFile == null)
            read();

        try {
            JSONArray jsonArray = jsonFile.getJSONArray(arrayKey);
            jsonArray.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Reading a whole JSON file
    private String readJSONFromFile() {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    //Writing a whole JSON Object to a file
    private void writeToFile(JSONObject jsonObject) {
        if (jsonObject != null) {
            try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(jsonObject.toString(1));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //An attempt (fairly successful) to make writing to the end of JSON
    public void writeToEndOfFile(JSONObject object) {
        //Adding an input Object to the Object which represents the whole JSON file. For other methods working purposes
        if (object != null) {
            //TODO: make mode check in every method!
            if (!isInit) {
                init();
            }
            if (mode != MODE.WRITE_ONLY_EOF) {
                addObjectToJSON(object, mainArrayKey);
            }

            StringBuilder stringBuilder = new StringBuilder();
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {

                long position = randomAccessFile.length() - 1;
                long writePosition = -1;
                if (position < 0)
                    writePosition = 0;
                boolean wasFileCloseBracket = false;
                boolean wasArrayCloseBracket = false;
                char readChar;
                while (position >= 0) {
                    randomAccessFile.seek(position);
                    readChar = (char) randomAccessFile.read();
                    //Log.i("Write to the end", "position: " + position + ", char: " + readChar);
                    if (readChar == '}' && wasFileCloseBracket) {
                        writePosition = position + 1;
                        stringBuilder.append(",");
                        break;
                    }
                    else if (readChar == '}' && !wasFileCloseBracket) {
                        wasFileCloseBracket = true;
                        position--;
                    }
                    else if (readChar == ']' && !wasArrayCloseBracket) {
                        wasArrayCloseBracket = true;
                        position--;
                    }
                    else if(readChar == '[' && wasArrayCloseBracket) {
                        writePosition = position + 1;
                        break;
                    }
                    else if (readChar == ' ' || readChar == '\n') {
                        position--;
                    }
                }

                //Log.i("Write to the end", "length: " + randomAccessFile.length() + ", writePosition: " + writePosition);
                if (writePosition >= 0) {
                    stringBuilder.append("\n");
                    stringBuilder.append(object.toString(1));
                    stringBuilder.append("\n]\n}");

                    randomAccessFile.seek(writePosition);
                    randomAccessFile.write(stringBuilder.toString().getBytes());
                }
                else {
                    Log.e("JsonIO", "Write to the end of file error");
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //Replaces object at index with new Object
    public void replaceObjectAtIndex(JSONObject newObject, int index, String arrayKey) throws ObjectNotFoundException {
        if (jsonFile == null) {
            read();
        }
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonFile.getJSONArray(arrayKey);
            if (index >= 0 && index < jsonArray.length()) {
                jsonArray.put(index, newObject);
                writeToFile(jsonFile);
            }
            else {
                throw new IllegalArgumentException();
            }
        } catch (JSONException e) {
            throw new ObjectNotFoundException();
        }

    }

    //Finds an Object with specified key-value pair and replaces it with new Object
    public void replaceObject(JSONObject newObject, String oldObjectSearchKey, String oldObjectSearchValue, String arrayKey) throws ObjectNotFoundException {
        if (jsonFile == null) {
            read();
        }
        try {
            JSONArray jsonArray = jsonFile.getJSONArray(arrayKey);
            int i = getIndexOfObject(oldObjectSearchKey, oldObjectSearchValue, arrayKey);
            jsonArray.put(i, newObject);
            writeToFile(jsonFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Finds an Object with specified key-value pair (outputs first object from the tail)
    public JSONObject searchObject(String objectSearchKey, String objectSearchValue, String arrayKey) throws ObjectNotFoundException {
        if (jsonFile == null) {
            read();
        }
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonFile.getJSONArray(arrayKey);
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.get(objectSearchKey).equals(objectSearchValue)) {
                    //Creating new object in order to not giving reference
                    return new JSONObject(jsonObject.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        throw new ObjectNotFoundException();
    }

    //Finds an Object with specified key-value pair (outputs first object from the tail)
    public JSONObject searchObjectAtIndex(int index, String arrayKey) throws ObjectNotFoundException {
        if (jsonFile == null) {
            read();
        }
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonFile.getJSONArray(arrayKey);
            if (index >= 0 && index < jsonArray.length()) {
                //Creating new object in order to not giving reference
                return new JSONObject(jsonArray.getJSONObject(index).toString());
            }
            else {
                throw new IllegalArgumentException();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        throw new ObjectNotFoundException();
    }

    //Finds an Object with specified key-value pair (outputs index of the first object from the tail)
    public int getIndexOfObject(String objectSearchKey, String objectSearchValue, String arrayKey) throws ObjectNotFoundException {
        if (jsonFile == null) {
            read();
        }
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonFile.getJSONArray(arrayKey);
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.get(objectSearchKey).equals(objectSearchValue)) {
                    return i;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        throw new ObjectNotFoundException();
    }

    public void deleteLast(String arrayKey) {
        if (jsonFile == null) {
            read();
        }
        try {
            JSONArray jsonArray = jsonFile.getJSONArray(arrayKey);
            if (jsonArray.length() > 0) {
                jsonArray.remove(jsonArray.length() - 1);
            }
            writeToFile(jsonFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteAt(int index, String arrayKey) {
        if (jsonFile == null) {
            read();
        }
        try {
            JSONArray jsonArray = jsonFile.getJSONArray(arrayKey);
            if (jsonArray.length() > index) {
                jsonArray.remove(index);
            }
            writeToFile(jsonFile);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList parseJsonArray(boolean isRereadJsonFile, ArrayList targetList, boolean isRewriteTargetList, String arrayKey, Class<?> className, Class<?>[] constructorArguments, String[] jsonKeys, @Nullable Object[] defaultValues) {
        JSONObject jsonObject;
        if (isRereadJsonFile || jsonFile == null) {
            jsonObject = read();
        }
        else {
            jsonObject = jsonFile;
        }
        if (isRewriteTargetList) {
            targetList = new ArrayList<>();
        }
        try {
            if (jsonObject.has(arrayKey)) {
                JSONArray jsonArray = jsonObject.getJSONArray(arrayKey);
                Constructor<?> constructor = className.getConstructor(constructorArguments);
                Object[] args = new Object[constructorArguments.length];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonStage = jsonArray.getJSONObject(i);
                    for (int j = 0; j < jsonKeys.length; j++) {
                        if (args.length > j) {
                            if (jsonStage.has(jsonKeys[j])) {
                                args[j] = jsonStage.get(jsonKeys[j]);
                            } else {
                                if (defaultValues != null && defaultValues.length > j)
                                    args[j] = defaultValues[j];
                                else throw new IllegalArgumentException();
                            }
                        }
                    }

                    try {
                        targetList.add(constructor.newInstance(args));
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IllegalArgumentException | NoSuchMethodException | JSONException e) {
            e.getMessage();
            e.printStackTrace();
        }
        return targetList;
    }

    public boolean isInit() {
        return isInit;
    }

    public static class ObjectNotFoundException extends Throwable {
    }

    public static enum MODE {
        READ_WRITE,
        WRITE_ONLY_EOF,
        WRITE,
        READ
    }
}
