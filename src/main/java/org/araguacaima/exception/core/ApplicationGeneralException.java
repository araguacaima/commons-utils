package org.araguacaima.exception.core;

import java.util.Calendar;
import java.util.Date;

public abstract class ApplicationGeneralException extends Throwable {

    public static final String PRINT_STACK_TRACE = "PRINT_STACK_TRACE";
    private static final long serialVersionUID = -4176197311797600886L;
    protected EnvironmentThrowableInfo environmentThrowableInfo;

    protected Date occurenceDateAndTime;

    protected ApplicationGeneralException() {
        super();
        occurenceDateAndTime = Calendar.getInstance().getTime();
    }

    protected ApplicationGeneralException(Throwable throwable) {
        super(throwable);
        occurenceDateAndTime = Calendar.getInstance().getTime();
    }

    protected ApplicationGeneralException(String message) {
        super(message);
        environmentThrowableInfo = new EnvironmentThrowableInfo(new Throwable(), this.getClass().getName());
    }

    protected ApplicationGeneralException(String message, Throwable throwable) {
        super(message, throwable);
        environmentThrowableInfo = new EnvironmentThrowableInfo(new Throwable(), this.getClass().getName());
    }

    public EnvironmentThrowableInfo getEnvironmentThrowableInfo() {
        return environmentThrowableInfo;
    }

    public Date getOccurrenceDateAndTime() {
        return occurenceDateAndTime;
    }
}
