/*
 * Copyright (C) 2021 Aarre Laakso
 *
 * This file is part of HoDoKu.
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package sudoku;

import java.util.logging.Logger;

/**
 * Catch exceptions thrown by Java threads that would otherwise be uncatchable.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private static final Logger logger = Logger.getLogger(ExceptionHandler.class.getName());

    ExceptionHandler() {
        super();
    }

    public final void uncaughtException(final Thread thread, final Throwable thrown)
    {
        ExceptionHandler.logger.severe(thread.toString());
        final StackTraceElement[] threadStackTrace = thread.getStackTrace();
        final int threadStackTraceLength = threadStackTrace.length;
        final StringBuilder threadLogMessage = new StringBuilder(threadStackTrace.length * 32);
        for (int i = 0; i < threadStackTraceLength; i++) {
            threadLogMessage.append(i);
            threadLogMessage.append(": ");
            threadLogMessage.append(threadStackTrace[i].toString());
            threadLogMessage.append("\n");
        }
        ExceptionHandler.logger.severe(threadLogMessage.toString());

        ExceptionHandler.logger.severe(thrown.toString());
        final StackTraceElement[] thrownStackTrace = thrown.getStackTrace();
        final int thrownStackTraceLength = thrownStackTrace.length;
        final StringBuilder thrownLogMessage = new StringBuilder(thrownStackTrace.length * 32);
        for (int i = 0; i < thrownStackTraceLength; i++) {
            thrownLogMessage.append(i);
            thrownLogMessage.append(": ");
            thrownLogMessage.append(thrownStackTrace[i].toString());
            thrownLogMessage.append("\n");
        }
        ExceptionHandler.logger.severe(thrownLogMessage.toString());
    }
}
