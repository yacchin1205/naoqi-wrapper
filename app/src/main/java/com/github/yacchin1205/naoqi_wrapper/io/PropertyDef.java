package com.github.yacchin1205.naoqi_wrapper.io;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiFunction;
import com.aldebaran.qi.sdk.util.FutureUtils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Definition of Property of NAOqi module
 *
 * Created by Satoshi on 2016/07/11.
 */
public class PropertyDef implements MemberDef {

    private int id;

    private String name;

    private TypeDef getType;

    private TypeDef setType;

    private List<?> help = null;

    public PropertyDef(List<String> params) {
        assert params.size() == 4;
        this.id = Integer.parseInt(params.get(0));
        this.name = params.get(1);
        this.getType = new TypeDef(params.get(2));
        this.setType = new TypeDef(params.get(3));
    }

    @Override
    public void write(PrintWriter out) throws IOException {
        VelocityContext ctx = new VelocityContext();
        ctx.put("typeGet", getType.asJavaClass(false));
        ctx.put("typeSet", setType.asJavaClass(true));
        ctx.put("comment", help.get(1));
        ctx.put("name", name);

        Template template = Velocity.getTemplate("property");
        template.merge(ctx, out);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s) %s", name, getType.asJavaClass(false), setType.asJavaClass(true)) + ' ' + help;
    }

    @Override
    public String getName() {
        return name;
    }

    public Future<Void> loadHelp(AnyObject module) {
        return module.call(List.class, "getMethodHelp", name).andThen(new QiFunction<Void, List>() {
            @Override
            public Future<Void> onResult(List list) throws Exception {
                help = list;
                return FutureUtils.wait(0, TimeUnit.MILLISECONDS);
            }
        });
    }

}
