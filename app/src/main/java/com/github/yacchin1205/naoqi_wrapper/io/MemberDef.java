package com.github.yacchin1205.naoqi_wrapper.io;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Definition of Member of NAOqi Module
 *
 * Created by Satoshi on 2016/07/11.
 */
public interface MemberDef {

    /**
     * Write java code
     *
     * @param out output stream for the code
     * @throws IOException
     */
    public void write(PrintWriter out) throws IOException;

    /**
     * Get member name
     *
     * @return member name
     */
    public String getName();

    /**
     * Load the help about the member from Runtime
     *
     * @param module Runtime object of NAOqi module
     * @return result
     */
    public Future<Void> loadHelp(AnyObject module);
}
