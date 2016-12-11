package com.vise.app;

import android.content.Intent;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        printMap();
        printList();
        printJson();
        printXml();
    }

    private void printXml() {
        String xml = "<dawi><xyy><td></td></xyy><tt></tt></dawi>";
        ViseLog.xml(xml);
    }

    private void printJson() {
        String json = "{'first':[{'t':'xx'},{'h':'ff'}],'second':{'third':'third','four':'four'}}";
        ViseLog.json(json);
    }

    private void printList() {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            list.add("dawi"+i);
        }
        ViseLog.d(list);
    }

    private void printMap() {
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < 5; i++){
            map.put("dawi"+i, "xyy"+i);
        }
        ViseLog.d(map);
    }
}
