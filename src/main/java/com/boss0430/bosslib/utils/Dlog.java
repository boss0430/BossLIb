package com.boss0430.bosslib.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Class that Display logs.<br>
 * Several variables set to public static. see'out' method to check reason.
 * @since 2019 Mar 27
 * @author boss0430
 * source comes from "Dbg.java" file in several projects.
 */
public class Dlog {

    // Test for Library project synchronization
    // Wow It Works!!
    // Test For 2nd Commit to GitHub

    private static boolean showLog = false;

    @SuppressWarnings("WeakerAccess")
    public static final int v = Log.VERBOSE;

    @SuppressWarnings("WeakerAccess")
    public static final int d = Log.DEBUG;

    @SuppressWarnings("WeakerAccess")
    public static final int i = Log.INFO;

    @SuppressWarnings("WeakerAccess")
    public static final int w = Log.WARN;

    @SuppressWarnings("WeakerAccess")
    public static final int e = Log.ERROR;

    @SuppressWarnings("WeakerAccess")
    public static final String verbose = "v";

    @SuppressWarnings("WeakerAccess")
    public static final String debug = "d";

    @SuppressWarnings("WeakerAccess")
    public static final String info = "i";

    @SuppressWarnings("WeakerAccess")
    public static final String warn = "w";

    @SuppressWarnings("WeakerAccess")
    public static final String error = "e";

    public Dlog() {
    }

    public static boolean isShowLog() { return showLog; }

    /**
     * Print Log to LogCat (DEBUG)
     * @param _classOrTag Context of caller / or / TAG string
     * @param _msg Message that wants to display
     * @author boss0430.
     * @since 2018 Jan 16
     * Simple Version of [tempout(Object, String, Object)]
     */
    public static void out(Object _classOrTag, String _msg) {

        if (!showLog) {
            return;
        }

        String tag;

        if (_classOrTag instanceof Context || _classOrTag instanceof Fragment) {
            tag = _classOrTag.getClass().getSimpleName();
        } else {
            tag = _classOrTag.toString();
        }

        // _msg = "[" + _msg + "]";
        Log.d(tag, _msg);

    } // ENDOF out FUNCTION.

    /**
     * Print Log to LogCat. You can use 1st param (Context/String) unlike old Project KT and so on.
     * @param _classOrTag Context of caller / or / TAG string
     * @param _msg Message that wants to display
     * @param _type Dbg.v,d,i,w,e / or / Dbg.verbose,debug,info,warn,error. Both style will work.
     * @author boss0430.
     * @since 2014 Sep 15
     * 2015 Apr 15 Add Fragment to tagname parameter.
     */
    public static void out(Object _classOrTag, String _msg, Object _type) {

        if (!showLog) {
            return;
        }

        String tag;
        String logType;

        if (_classOrTag instanceof Context || _classOrTag instanceof Fragment) {
            tag = _classOrTag.getClass().getSimpleName();
        } else {
            tag = _classOrTag.toString();
        }

        if (_type instanceof String) {
            logType = _type.toString();
        } else {
            logType = convertLogType((Integer) _type);
        }

        // _msg = "[" + _msg + "]";

        if (verbose.equalsIgnoreCase(logType)) {
            Log.v(tag, _msg);
        } else if (debug.equalsIgnoreCase(logType)) {
            Log.d(tag, _msg);
        } else if (info.equalsIgnoreCase(logType)) {
            Log.i(tag, _msg);
        } else if (warn.equalsIgnoreCase(logType)) {
            Log.w(tag, _msg);
        } else if (error.equalsIgnoreCase(logType)) {
            Log.e(tag, _msg);
        }
    } // ENDOF out FUNCTION.


    /**
     * Print Log to LogCat. You can use 1st param (Context/String) unlike old Project KT and so on.
     * @param _classOrTag Context of caller / or / TAG string
     * @param _msg Message that wants to display
     * @param _type Dbg.v,d,i,w,e / or / Dbg.verbose,debug,info,warn,error. Both style will work.
     * @author boss0430.
     * @since 2014 Sep 15
     * 2015 Apr 15 Add Fragment to tagname parameter.
     */
    public static void detailOut(Object _classOrTag, String _msg, Object _type) {

        if (!showLog) {
            return;
        }

        String tag;
        String logType;

        if (_classOrTag instanceof Context || _classOrTag instanceof Fragment) {
            tag = _classOrTag.getClass().getSimpleName();
        } else {
            tag = _classOrTag.toString();
        }

        if (_type instanceof String) {
            logType = _type.toString();
        } else {
            logType = convertLogType((Integer) _type);
        }

        StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
        int currentIndex = -1;
        for (int i = 0; i < stackTraceElement.length; i++) {
            if (stackTraceElement[i].getMethodName().compareTo("out") == 0) {
                currentIndex = i + 1;
                break;
            }
        }

        if (currentIndex > 0) {
            String fullClassName = stackTraceElement[currentIndex].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = stackTraceElement[currentIndex].getMethodName();
            String lineNumber = String.valueOf(stackTraceElement[currentIndex].getLineNumber());

            // Log.i(tag, msg);
            // Log.i(tag + " position", "at " + fullClassName + "." + methodName +
            // "(" + className + ".java:" + lineNumber + ")");

            if (verbose.equalsIgnoreCase(logType)) {
                Log.v(tag, _msg);
                Log.v(tag + " position", "at " + fullClassName + "." + methodName + "(" + className + ".java:" + lineNumber + ")");
            } else if (debug.equalsIgnoreCase(logType)) {
                Log.d(tag, _msg);
                Log.d(tag + " position", "at " + fullClassName + "." + methodName + "(" + className + ".java:" + lineNumber + ")");
            } else if (info.equalsIgnoreCase(logType)) {
                Log.i(tag, _msg);
                Log.i(tag + " position", "at " + fullClassName + "." + methodName + "(" + className + ".java:" + lineNumber + ")");
            } else if (warn.equalsIgnoreCase(logType)) {
                Log.w(tag, _msg);
                Log.w(tag + " position", "at " + fullClassName + "." + methodName + "(" + className + ".java:" + lineNumber + ")");
            } else if (error.equalsIgnoreCase(logType)) {
                Log.e(tag, _msg);
                Log.e(tag + " position", "at " + fullClassName + "." + methodName + "(" + className + ".java:" + lineNumber + ")");
            }
        }
    }

    private static String convertLogType(int _typenum) {
        String rtnStr = "";
        switch (_typenum) {
            case v:
                rtnStr = verbose;
                break;
            case d:
                rtnStr = debug;
                break;
            case i:
                rtnStr = info;
                break;
            case w:
                rtnStr = warn;
                break;
            case e:
                rtnStr = error;
                break;
            default:
                break;
        }
        return rtnStr;
    }
}
