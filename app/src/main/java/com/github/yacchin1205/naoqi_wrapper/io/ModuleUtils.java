package com.github.yacchin1205.naoqi_wrapper.io;

import android.content.Context;
import android.util.Log;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.FutureFunction;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.QiFunction;

import org.apache.velocity.app.Velocity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ix.Ix;
import rx.functions.Func1;

/**
 * Inspector for NAOqi ModuleUtils
 *
 * Created by Satoshi on 2016/07/11.
 */
public class ModuleUtils {

    public static void init(Context ctx) {
        Velocity.setProperty("resource.loader", "android");
        Velocity.setProperty("android.resource.loader.class", TemplateLoader.class.getName());
        Velocity.setProperty("android.content.res.Resources", ctx.getResources());
        Velocity.init();
    }

    public static Future<List<MemberDef>> loadMembers(final AnyObject o) {
        final List<MemberDef> members = Ix.from(Arrays.asList(o.toString().split("\n"))).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return s.replaceAll("\u001B\\[[;\\d]*m", "");
            }
        }).skip(1).map(new Func1<String, List<String>>() {
            @Override
            public List<String> call(String s) {
                return Arrays.asList(s.trim().split("\\s+"));
            }
        }).map(new Func1<List<String>, MemberDef>() {
            @Override
            public MemberDef call(List<String> strings) {
                if (strings.get(3).startsWith("(")) {
                    return new MethodDef(strings);
                } else {
                    return new PropertyDef(strings);
                }
            }
        }).into(new ArrayList<MemberDef>());

        return Future.waitAll(Ix.from(members).map(new Func1<MemberDef, Future<Void>>() {
            @Override
            public Future<Void> call(MemberDef memberDef) {
                return memberDef.loadHelp(o);
            }
        }).into(new ArrayList<Future<Void>>()).toArray(new Future[0])).then(new QiFunction<List<MemberDef>, Void>() {
            @Override
            public Future<List<MemberDef>> onResult(Void o) throws Exception {
                Promise<List<MemberDef>> p = new Promise<List<MemberDef>>();
                p.setValue(members);
                return p.getFuture();
            }
        });
    }

}
