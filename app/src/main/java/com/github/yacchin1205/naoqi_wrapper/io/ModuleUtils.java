package com.github.yacchin1205.naoqi_wrapper.io;

import android.content.Context;
import android.util.Log;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.FutureFunction;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.QiFunction;
import com.github.yacchin1205.naoqi_wrapper.MainActivity;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.IncludeEventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ix.Ix;
import ix.Pair;
import rx.functions.Func1;
import rx.functions.Func2;

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
        final String[] descs = o.toString().split("\n");
        final Ix<Pair<Integer, List<String>>> lines = Ix.from(Arrays.asList(descs)).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return s.replaceAll("\u001B\\[[;\\d]*m", "");
            }
        }).mapIndexed(new Func2<Integer, String, Pair<Integer, List<String>>>() {
            @Override
            public Pair<Integer, List<String>> call(Integer index, String s) {
                return new Pair<Integer, List<String>>(index, Arrays.asList(s.trim().split("\\s+")));
            }
        });
        final Ix<Pair<Integer, String>> headers = lines.filter(new Func1<Pair<Integer, List<String>>, Boolean>() {
            @Override
            public Boolean call(Pair<Integer, List<String>> a) {
                return a.second.get(0).equals("*");
            }
        }).map(new Func1<Pair<Integer, List<String>>, Pair<Integer, String>>() {
            @Override
            public Pair<Integer, String> call(Pair<Integer, List<String>> integerListPair) {
                return new Pair<Integer, String>(integerListPair.first, integerListPair.second.get(1));
            }
        }).concatWith(new Pair<Integer, String>(descs.length, null));
        final Ix<Pair<String, Pair<Integer,Integer>>> headerRanges = headers.zipWith(headers.skip(1),
                new Func2<Pair<Integer, String>, Pair<Integer, String>, Pair<String, Pair<Integer, Integer>>>() {
                    @Override
                    public Pair<String, Pair<Integer, Integer>> call(Pair<Integer, String> a1, Pair<Integer, String> a2) {
                        return new Pair<String, Pair<Integer, Integer>>(a1.second,
                                new Pair<Integer, Integer>(a1.first + 1, a2.first - a1.first - 1));
                    }
                });
        Log.i(MainActivity.TAG, "Headers("+ descs.length +" lines): " + headerRanges.into(new ArrayList<Pair<String, Pair<Integer, Integer>>>()));

        final List<MemberDef> members = headerRanges.map(new Func1<Pair<String,Pair<Integer,Integer>>, Ix<MemberDef>>() {
            @Override
            public Ix<MemberDef> call(Pair<String, Pair<Integer, Integer>> a) {
                if (a.first.equals("Methods:")) {
                    return lines.skip(a.second.first).take(a.second.second).map(new Func1<Pair<Integer, List<String>>, MemberDef>() {
                        @Override
                        public MemberDef call(Pair<Integer, List<String>> strings) {
                            if (strings.second.get(3).startsWith("(")) {
                                return new MethodDef(strings.second);
                            } else {
                                return new PropertyDef(strings.second);
                            }
                        }
                    });
                }else{
                    return lines.skip(a.second.first).take(a.second.second).map(new Func1<Pair<Integer, List<String>>, MemberDef>() {
                        @Override
                        public MemberDef call(Pair<Integer, List<String>> strings) {
                            return new SignalDef(strings.second);
                        }
                    });
                }
            }
        }).flatMap(new Func1<Ix<MemberDef>, Iterable<MemberDef>>() {
            @Override
            public Iterable<MemberDef> call(Ix<MemberDef> memberDefs) {
                return memberDefs;
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
