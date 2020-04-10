
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileHandler {
    private Context parent;

    public FileHandler(Context c){
        parent = c;
    }

    public String readFile(String fileName){
        if(!fileExists(fileName)){
            return null;
        }

        try {
            FileInputStream fis = parent.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException fileNotFound) {
            return null;
        }
    }

    public boolean writeStringToFile(String inputObject, String fileName){
        try {
            FileOutputStream fos = parent.openFileOutput(fileName,Context.MODE_PRIVATE);
            if (inputObject != null) {
                fos.write(inputObject.getBytes());
            }
            fos.close();
            return true;
        } catch (IOException fileNotFound) {
            return false;
        }
    }

    private boolean fileExists(String filename) {
        File file = parent.getFileStreamPath(filename);
        return file != null && file.exists();
    }
}
