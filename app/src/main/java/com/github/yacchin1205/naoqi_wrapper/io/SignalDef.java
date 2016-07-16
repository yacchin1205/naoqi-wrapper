package com.github.yacchin1205.naoqi_wrapper.io;

import android.text.TextUtils;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiFunction;
import com.aldebaran.qi.sdk.util.FutureUtils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ix.Ix;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Definition of Method of NAOqi module
 *
 * Created by Satoshi on 2016/07/11.
 */
public class SignalDef implements MemberDef {

    private int id;

    private String name;

    private List<TypeDef> paramTypes;

    public SignalDef(List<String> params) {
        assert params.size() == 4;
        this.id = Integer.parseInt(params.get(0));
        this.name = params.get(1);
        assert params.get(2).startsWith("(") && params.get(2).endsWith(")");
        String arg = params.get(2).replaceAll("\\<([^,]+),([^,]+)\\>", "<$1 $2>");
        this.paramTypes = Ix.from(Arrays.asList(arg.length() > 2 ? arg.substring(1, arg.length() - 1).split(",") : new String[0]))
                .map(new Func1<String, TypeDef>() {
                    @Override
                    public TypeDef call(String s) {
                        return new TypeDef(s);
                    }
                }).into(new ArrayList<TypeDef>());
    }

    @Override
    public void write(PrintWriter out) throws IOException {
        VelocityContext ctx = new VelocityContext();
        ctx.put("name", name);
        ctx.put("paramTypes", TextUtils.join(", ", Ix.from(paramTypes).map(new Func1<TypeDef, String>() {
            @Override
            public String call(TypeDef o) {
                return o.asJavaClass(true);
            }
        })));

        Template template = Velocity.getTemplate("signal");
        template.merge(ctx, out);
    }

    @Override
    public Future<Void> loadHelp(AnyObject module) {
                return FutureUtils.wait(0, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return String.format("(signal) %s(%s)", name, Ix.from(paramTypes).map(new Func1<TypeDef, String>() {
            @Override
            public String call(TypeDef typeDef) {
                return typeDef.asJavaClass(true);
            }
        }).into(new ArrayList<String>()));
    }

    @Override
    public String getName() {
        return name;
    }
}
