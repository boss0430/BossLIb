package com.boss0430.bosslib.location;

public interface LocationTaskInformer {

    /**
     * Invoked in 'onPostExecute' in LocationAsyncTask.
     * implements in your caller.
     * @param result
     */
    void onTaskDone(String result);
}
