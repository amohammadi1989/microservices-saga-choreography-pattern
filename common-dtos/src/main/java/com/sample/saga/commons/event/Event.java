package com.sample.saga.commons.event;

import java.util.Date;
import java.util.UUID;

public interface Event {

    UUID getEventId();
    
    Date getDate();
}
