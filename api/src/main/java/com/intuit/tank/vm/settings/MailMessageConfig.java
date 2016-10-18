package com.intuit.tank.vm.settings;

/*
 * #%L
 * Intuit Tank Api
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */


import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <message event="QUEUE_ADD"> <subject>Job {jobName} Added to Queue</subject> <body> <![CDATA[
 * <p>
 * The Job {jobName} for Project {projectName} was added to the job queue at {eventTime}.
 * </p>
 * ]]> </body> </message>
 * 
 * <br>
 * 
 * @author dangleton
 */
@ApplicationScoped
public class MailMessageConfig extends BaseCommonsXmlConfig {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogManager.getLogger(MailMessageConfig.class);
    private static final String KEY_MESSAGE_NODE = "message";
    private static final String KEY_EVENT = "@event";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_FOOTER = "footer";
    private static final String KEY_BODY = "body";
    private static final String KEY_CSS = "css-style";

    private static final String DEFAULT = "default";

    private static final String CONFIG_NAME = "mail-messages.xml";
    private static String configName = CONFIG_NAME;

    static {
        File file = new File(configName);
        LOG.info("checking file " + file.getAbsolutePath() + ": exists=" + file.exists());
        if (!file.exists()) {
            LOG.info("System.getenv('WATS_PROPERTIES') = '" + System.getenv("WATS_PROPERTIES") + "'");
            LOG.info("System.getProperty('WATS_PROPERTIES') = '" + System.getProperty("WATS_PROPERTIES") + "'");
            if (System.getenv("WATS_PROPERTIES") != null) {
                configName = System.getenv("WATS_PROPERTIES") + "/" + CONFIG_NAME;
            } else if (System.getProperty("WATS_PROPERTIES") != null) {
                configName = System.getProperty("WATS_PROPERTIES") + "/" + CONFIG_NAME;
            }
        }
    }

    private String configPath = configName;

    private Map<String, MailMessage> messages = new HashMap<String, MailMessage>();

    /**
     * 
     */
    MailMessageConfig(String configPath) {
        this.configPath = configPath;
        readConfig();
    }

    /**
     * private constructor to implement the singleton pattern
     * 
     */
    public MailMessageConfig() {
        readConfig();
    }

    public MailMessage getMailMessage(String event) {
        checkReload();
        MailMessage mailMessage = messages.get(event);
        if (mailMessage == null) {
            mailMessage = messages.get(MailMessageConfig.DEFAULT);
        }
        return mailMessage;
    }

    public Collection<MailMessage> getAllMessages() {
        checkReload();
        return messages.values();
    }

    /**
     * @return the footer
     */
    public String getFooter() {
        return config
                .getString(
                        KEY_FOOTER,
                        "<p class=\"footer\">This message automatically generated by Tank (<a href=\"{tankBaseUrl}\">{tankBaseUrl}</a>). </p>");
    }

    public String getStyle() {
        return config.getString(KEY_CSS, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigName() {
        return configPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initConfig(XMLConfiguration configuration) {
        messages.clear();
        if (config != null) {
            String style = getStyle();
            @SuppressWarnings("unchecked") List<HierarchicalConfiguration> msgConfigs = config
                    .configurationsAt(KEY_MESSAGE_NODE);
            for (HierarchicalConfiguration msgConfig : msgConfigs) {
                String event = msgConfig.getString(KEY_EVENT);
                String subject = msgConfig.getString(KEY_SUBJECT);
                String body = msgConfig.getString(KEY_BODY);
                if (!StringUtils.isEmpty(event) && !StringUtils.isEmpty(subject) && !StringUtils.isEmpty(body)) {
                    messages.put(event, new MailMessage(body, subject, style));
                }
            }
        }
    }

}
