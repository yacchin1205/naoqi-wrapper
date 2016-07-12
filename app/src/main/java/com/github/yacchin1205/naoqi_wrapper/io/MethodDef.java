package com.github.yacchin1205.naoqi_wrapper.io;

import android.text.TextUtils;
import android.util.Log;

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
public class MethodDef implements MemberDef {

    private int id;

    private String name;

    private TypeDef retType;

    private List<TypeDef> paramTypes;

    private List<?> help = null;

    public MethodDef(List<String> params) {
        assert params.size() == 4;
        this.id = Integer.parseInt(params.get(0));
        this.name = params.get(1);
        this.retType = new TypeDef(params.get(2));
        assert params.get(3).startsWith("(") && params.get(3).endsWith(")");
        String arg = params.get(3).replaceAll("\\<([^,]+),([^,]+)\\>", "<$1 $2>");
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
        ctx.put("typeRet", retType.asJavaClass(false));
        List<List<String>> paramHelp = (List<List<String>>) help.get(2);
        Ix<List<String>> paramWithNames = Ix.from(paramTypes)
                .zipWith(paramHelp, new Func2<TypeDef, List<String>, List<String>>() {
                    @Override
                    public List<String> call(TypeDef typeDef, List<String> strings) {
                        return Arrays.asList(new String[]{typeDef.asJavaClass(true), strings.get(0), strings.get(1) + " - " + typeDef.toString()});
                    }
                });
        if(paramHelp.size() < paramTypes.size()) {
            paramWithNames = paramWithNames.concatWith(Ix.range(paramHelp.size() + 1, paramTypes.size() + 1)
                    .zipWith(paramTypes.subList(paramHelp.size(), paramTypes.size()), new Func2<Integer, TypeDef, List<String>>() {
                        @Override
                        public List<String> call(Integer integer, TypeDef typeDef) {
                            return Arrays.asList(new String[]{typeDef.asJavaClass(true), " p" + integer, "(None) - " + typeDef.toString()});
                        }
                    }));
        }
        ctx.put("paramDecls", TextUtils.join(", ", paramWithNames.map(new Func1<List<String>, String>() {
            @Override
            public String call(List<String> strings) {
                return strings.get(0) + " " + strings.get(1);
            }
        }).into(new ArrayList<String>())));
        ctx.put("paramNames", TextUtils.join(", ", paramWithNames.map(new Func1<List<String>, String>() {
            @Override
            public String call(List<String> strings) {
                return strings.get(1);
            }
        }).into(new ArrayList<String>())));
        String comment = help.get(1) + "\n     * \n";
        for(String param : paramWithNames.map(new Func1<List<String>, String>() {
            @Override
            public String call(List<String> strings) {
                return "     * @param " + strings.get(1) + " " + strings.get(2);
            }
        })) {
            comment += param + "\n";
        }
        comment += "     * @return " + help.get(4) + " - " + retType.toString();
        ctx.put("comment", comment);
        ctx.put("name", name);

        Template template = Velocity.getTemplate("method");
        template.merge(ctx, out);
    }

    @Override
    public Future<Void> loadHelp(AnyObject module) {
        return module.call(List.class, "getMethodHelp", name).andThen(new QiFunction<Void, List>() {
            @Override
            public Future<Void> onResult(List list) throws Exception {
                help = list;
                return FutureUtils.wait(0, TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public String toString() {
        return String.format("%s %s(%s)", retType.asJavaClass(false), name, Ix.from(paramTypes).map(new Func1<TypeDef, String>() {
            @Override
            public String call(TypeDef typeDef) {
                return typeDef.asJavaClass(true);
            }
        }).into(new ArrayList<String>())) + ' ' + help;
    }

    @Override
    public String getName() {
        return name;
    }
}
