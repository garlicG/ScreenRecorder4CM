package com.garlicg.screenrecordct.data;

import android.os.AsyncTask;

public abstract class AsyncExecutor<Params, Progress , Result> extends AsyncTask<Params , Progress ,Result> {

    private Listener<Result> mListener;
    public AsyncExecutor(Listener<Result> listener){
        mListener = listener;
    }

    @Override
    protected void onPostExecute(Result result) {
        mListener.onPostExecute(result);
    }

    public interface Listener<Result> {
        void onPostExecute(Result result);
    }

}