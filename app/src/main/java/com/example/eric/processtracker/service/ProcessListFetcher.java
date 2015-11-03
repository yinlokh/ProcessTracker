package com.example.eric.processtracker.service;

import com.example.eric.processtracker.model.ProcessEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ProcessListFetcher {

    public ArrayList<ProcessEntry> fetchProcesses() {
        return parseResult(getPSResultString());
    }

    private String getPSResultString() {
        String ret = "";
        StringBuffer output = new StringBuffer();
        try {
            java.lang.Process ps = Runtime.getRuntime().exec("top -n 1 -d 0");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ps.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            while((read = reader.read(buffer)) > 0) {
                output.append(buffer,0,read);
            }
            reader.close();
            ps.waitFor();
            ret = output.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e){

        }
        return ret;
    }

    private ArrayList<ProcessEntry> parseResult(String result){
        ArrayList<ProcessEntry> newProcesses = new ArrayList<ProcessEntry>();
        String[] lines = result.split("\n");
        for(int i = 7; i < lines.length; i++){
            String[] data = lines[i].trim().split("[\\s]+");

            // skip root items (kworker/kthread...)
            if (data[data.length-2].equals("root")) {
                continue;
            }
            newProcesses.add(new ProcessEntry(
                    Integer.parseInt(data[0]),
                    Integer.parseInt(data[5].split("K")[0]),
                    data[data.length-1]));
        }
        return newProcesses;
    }
}