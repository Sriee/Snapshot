package com.aos.log;

import java.util.ArrayList;
import java.util.Collection;

public interface Logger {
    public void writeEntry(ArrayList<int[]> entry);
    public void writeEntry(String entry);
    public void writeLog(Collection<String> entry);
    public void writeLog(String entry);
}
