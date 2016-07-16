package com.github.yacchin1205.naoqi_wrapper.io;

import com.aldebaran.qi.AnyObject;

import java.util.List;
import java.util.Map;

/**
 * Definition of Types of NAOqi module
 * 
 * Created by Satoshi on 2016/07/11.
 */
public class TypeDef {

    private String name;

    public TypeDef(String name) {
        this.name = name;
    }

    public String asJavaClass(boolean forParameter) {
        if (name.equals("Int32")) {
            if (forParameter) {
                return Integer.TYPE.getName();
            } else {
                return Integer.class.getName();
            }
        } else if (name.equals("UInt64")) {
            if (forParameter) {
                return Long.TYPE.getName();
            } else {
                return Long.class.getName();
            }
        } else if (name.equals("Float")) {
            if (forParameter) {
                return Float.TYPE.getName();
            } else {
                return Float.class.getName();
            }
        } else if (name.equals("String")) {
            return String.class.getName();
        } else if (name.equals("Bool")) {
            if (forParameter) {
                return Boolean.TYPE.getName();
            } else {
                return Boolean.class.getName();
            }
        } else if (name.equals("Void")) {
            return Void.class.getName();
        } else if (name.equals("Value")) {
            return Object.class.getName();
        } else if (name.equals("Object")) {
            return AnyObject.class.getName();
        } else if (name.startsWith("List<") && name.endsWith(">")) {
            if(forParameter) {
                return List.class.getName() + "<" + (new TypeDef(name.substring(5, name.length() - 1)).asJavaClass(forParameter)) + ">";
            }else{
                return List.class.getName();
            }
        } else if (name.startsWith("Map<") && name.endsWith(">")) {
            if(forParameter) {
                String[] types = name.substring(4, name.length() - 1).split(" ");
                return Map.class.getName() + "<" + (new TypeDef(types[0].trim()).asJavaClass(forParameter)) + "," + (new TypeDef(types[1].trim()).asJavaClass(forParameter)) + ">";
            }else{
                return Map.class.getName();
            }
        } else if (name.equals("Unknown")) {
            return Object.class.getName();
        } else {
            throw new IllegalStateException("Unknown type: " + name);
        }
    }

    @Override
    public String toString() {
        return name;
    }

}


