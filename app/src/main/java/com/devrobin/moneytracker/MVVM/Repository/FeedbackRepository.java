package com.devrobin.moneytracker.MVVM.Repository;

import android.content.Context;

import com.devrobin.moneytracker.MVVM.DAO.FeedbackDAO;
import com.devrobin.moneytracker.MVVM.Model.FeedbackModel;
import com.devrobin.moneytracker.MVVM.TransactionDatabase;

public class FeedbackRepository {

    private final FeedbackDAO feedbackDao;

    public FeedbackRepository(Context context) {
        this.feedbackDao = TransactionDatabase.getInstance(context).feedbackDao();
    }

    public long insertFeedback(FeedbackModel feedback) {
        return feedbackDao.insertFeedback(feedback);
    }

}
