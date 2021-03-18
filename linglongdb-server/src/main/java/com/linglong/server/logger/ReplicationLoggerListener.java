package com.linglong.server.logger;

import com.linglong.engine.event.EventListener;
import com.linglong.engine.event.EventType;
import com.linglong.server.config.LinglongdbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liuj-ai on 2021/3/18.
 */
public class ReplicationLoggerListener implements EventListener {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ReplicationLoggerListener.class);

    private LinglongdbProperties linglongdbProperties;

    public ReplicationLoggerListener(LinglongdbProperties linglongdbProperties) {
        this.linglongdbProperties = linglongdbProperties;
    }

    @Override
    public void notify(EventType type, String message, Object... args) {
        LOGGER.info("Node[{}] type: {} message: {} args: {}", linglongdbProperties.getReplicaPort(), type.toString(), message, args);
    }
}
