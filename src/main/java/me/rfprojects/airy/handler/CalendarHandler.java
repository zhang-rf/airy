package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CalendarHandler implements Handler {

    private static final long DEFAULT_GREGORIAN_CUTOVER = -12219292800000L;

    @Override
    public boolean supportsType(Class<?> type) {
        return type == Calendar.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Calendar calendar = (Calendar) object;
        buffer.putString(calendar.getTimeZone().getID());
        buffer.putUnsignedVarint(calendar.getTimeInMillis());
        buffer.putBoolean(calendar.isLenient());
        buffer.putUnsignedVarint(calendar.getFirstDayOfWeek());
        buffer.putUnsignedVarint(calendar.getMinimalDaysInFirstWeek());
        if (object instanceof GregorianCalendar)
            buffer.putUnsignedVarint(((GregorianCalendar) object).getGregorianChange().getTime());
        else
            buffer.putUnsignedVarint(DEFAULT_GREGORIAN_CUTOVER);
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(buffer.getString()));
        calendar.setTimeInMillis(buffer.getUnsignedVarint());
        calendar.setLenient(buffer.getBoolean());
        calendar.setFirstDayOfWeek((int) buffer.getUnsignedVarint());
        calendar.setMinimalDaysInFirstWeek((int) buffer.getUnsignedVarint());
        long gregorianChange = buffer.getUnsignedVarint();
        if (gregorianChange != DEFAULT_GREGORIAN_CUTOVER)
            if (calendar instanceof GregorianCalendar)
                ((GregorianCalendar) calendar).setGregorianChange(new Date(gregorianChange));
        return calendar;
    }
}
