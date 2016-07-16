package com.github.yacchin1205.naoqi_wrapper.io;

import android.content.res.Resources;
import android.util.Log;

import com.github.yacchin1205.naoqi_wrapper.R;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import java.io.InputStream;

/**
 * Template loader for Velocity
 *
 * Created by Satoshi on 2016/07/11.
 */
public class TemplateLoader extends FileResourceLoader {
    private Resources resources;

    public void commonInit(RuntimeServices rs, ExtendedProperties configuration) {
        super.commonInit(rs, configuration);
        this.resources = (Resources) rs.getProperty("android.content.res.Resources");
    }

    public long getLastModified(Resource resource) {
        return 0L;
    }

    public InputStream getResourceStream(String templateName) {
        return resources.openRawResource(getIdentifier(templateName));
    }

    public boolean isSourceModified(Resource resource) {
        return false;
    }

    public boolean resourceExists(String templateName) {
        return getIdentifier(templateName) != 0;
    }

    private int getIdentifier(String templateName) {
        if (templateName.equals("method")) {
            return R.raw.method;
        } else if (templateName.equals("property")) {
            return R.raw.property;
        } else if (templateName.equals("signal")) {
            return R.raw.signal;
        } else if (templateName.equals("header")) {
            return R.raw.header;
        } else if (templateName.equals("footer")) {
            return R.raw.footer;
        } else {
            Log.e("Sample", "Unknown template: " + templateName);
            return 0;
        }
    }
}