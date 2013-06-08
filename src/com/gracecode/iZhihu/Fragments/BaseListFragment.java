package com.gracecode.iZhihu.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import com.gracecode.iZhihu.Activity.Detail;
import com.gracecode.iZhihu.Adapter.QuestionsAdapter;
import com.gracecode.iZhihu.Dao.Question;
import com.gracecode.iZhihu.Dao.QuestionsDatabase;
import com.handmark.pulltorefresh.extras.listfragment.PullToRefreshListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;

public abstract class BaseListFragment extends PullToRefreshListFragment implements AdapterView.OnItemClickListener {
    private static final String SAVED_QUESTIONS = "savedQuestions";

    Context context;
    QuestionsAdapter questionsAdapter;
    private Activity activity;
    QuestionsDatabase questionsDatabase;
    ArrayList<Question> questions;

    SharedPreferences sharedPref;
    protected PullToRefreshListView pull2RefreshView;

    BaseListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.activity = getActivity();
        this.context = activity.getApplicationContext();
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // @todo 初始化数据库
        this.questionsDatabase = new QuestionsDatabase(context);

        if (savedInstanceState != null) {
            this.questions = savedInstanceState.getParcelableArrayList(SAVED_QUESTIONS);
        }

        if (this.questions == null || this.questions.size() <= 0) {
            this.questions = getInitialData();
        }
        this.questionsAdapter = new QuestionsAdapter(context, questions);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (questions != null && questions.size() > 0) {
            outState.putParcelableArrayList(SAVED_QUESTIONS, questions);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // 默认关闭下拉
        pull2RefreshView.setMode(PullToRefreshBase.Mode.DISABLED);
    }

    @Override
    public void onResume() {
        if (questionsAdapter != null) {
            questionsAdapter.notifyDataSetChanged();
        }

        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setListAdapter(questionsAdapter);

        // 下拉 ListView 控件
        this.pull2RefreshView = getPullToRefreshListView();
        getListView().setOnItemClickListener(this);

        super.onActivityCreated(savedInstanceState);
    }


    /**
     * Initial Data
     *
     * @return An Empty ArrayList By Default.
     */
    ArrayList<Question> getInitialData() {
        return new ArrayList<>();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        // Android-PullToRefresh 似乎增加了个不可见条目，所以要 -1
        int selectedPosition = (pull2RefreshView != null) ? position - 1 : position;
        Question question = questions.get(selectedPosition);

        Intent intent = new Intent(activity, Detail.class);
        intent.putExtra(Detail.INTENT_EXTRA_CURRENT_POSITION, selectedPosition);
        intent.putExtra(Detail.INTENT_EXTRA_CURRENT_QUESTION, question);
        intent.putExtra(Detail.INTENT_EXTRA_QUESTIONS, questions);

        startActivityForResult(intent, Intent.FILL_IN_PACKAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Intent.FILL_IN_PACKAGE) {
            questions = data.getParcelableArrayListExtra(Detail.INTENT_MODIFIED_LISTS);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Get Recent Questions By Update Time.
     *
     * @return questions
     */
    public ArrayList<Question> getRecentQuestion() {
        return questionsDatabase.getRecentQuestions();
    }


    /**
     * Get Stared Questions.
     *
     * @return questions
     */
    public ArrayList<Question> getStaredQuestions() {
        return questionsDatabase.getStaredQuestions();
    }

    @Override
    public void onDestroy() {
        questionsDatabase.close();
        super.onDestroy();
    }
}
