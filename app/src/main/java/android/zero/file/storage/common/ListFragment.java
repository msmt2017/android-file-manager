

package android.zero.file.storage.common;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Looper;
import android.zero.R;
import android.zero.file.storage.common.BaseFragment;
import android.zero.file.storage.misc.CrashReportingManager;

public class ListFragment extends BaseFragment {
	private ListAdapter mAdapter;
    final private Handler mHandler = new Handler(Looper.getMainLooper());
    private ListView mList;
    private View mListContainer;
    private boolean mListShown;
    private View mProgressContainer;
    final private Runnable mRequestFocus = new Runnable() {
        @Override
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };
    private TextView mStandardEmptyView;
    private TextView mLoadingView;
    private CharSequence mEmptyText;
    private View mEmptyView;

    private void ensureList() {
        if (mList != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            return;
        }
        if (root instanceof ListView) {
            mList = (ListView) root;
        } else {
            mStandardEmptyView = (TextView) root
                    .findViewById(R.id.internalEmpty);
            if (mStandardEmptyView == null) {
                mEmptyView = root.findViewById(android.R.id.empty);
            } else {
                mStandardEmptyView.setVisibility(View.GONE);
            }
            mProgressContainer = root.findViewById(R.id.progressContainer);
            mLoadingView = (TextView) root.findViewById(R.id.loading);
            mListContainer = root.findViewById(R.id.listContainer);
            View rawListView = root.findViewById(R.id.list);
            if (rawListView == null) {
                throw new RuntimeException(
                        "Your content must have a ListView whose id attribute is "
                                + "'android.R.id.list'");
            }
            else{
            	try {
                	@SuppressWarnings("unused")
					ListView list = (ListView) rawListView;	
				} catch (Exception e) {
                    CrashReportingManager.logException(e);
		               throw new RuntimeException(
		                        "Content has view with id attribute 'android.R.id.list' "
		                                + "that is not a ListView class");
				}
            }
            mList = (ListView) rawListView;
            if (mEmptyView != null) {
                mList.setEmptyView(mEmptyView);
            } else if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
                mList.setEmptyView(mStandardEmptyView);
            }
        }
        mListShown = true;
        if (mAdapter != null) {
            ListAdapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        } else {
            if (mProgressContainer != null) {
                setListShown(false, false);
            }
        }
        mHandler.post(mRequestFocus);
    }

    protected View getEmptyView() {
        return mEmptyView;
    }

    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    public ListView getListView() {
        ensureList();
        return mList;
    }

    public long getSelectedItemId() {
        ensureList();
        return mList.getSelectedItemId();
    }

    public int getSelectedItemPosition() {
        ensureList();
        return mList.getSelectedItemPosition();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_content, container, false);
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mListShown = false;
        mProgressContainer = mListContainer = null;
        super.onDestroyView();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	ensureList();
    }

    public void setEmptyText(CharSequence text) {
        ensureList();
        if (mStandardEmptyView == null) {
//            throw new IllegalStateException(
//                    "Can't be used with a custom content view");
        }
        mStandardEmptyView.setText(text);
        if (mEmptyText == null) {
            mList.setEmptyView(mStandardEmptyView);
        }
        mEmptyText = text;
    }
    
    private void setLoadingText(CharSequence text) {
        ensureList();
        if (mLoadingView == null) {
//            throw new IllegalStateException(
//                    "Can't be used with a custom content view");
        	return;
        }
        mLoadingView.setText(text);
    }

    public void setListAdapter(ListAdapter adapter) {
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mList != null) {
            mList.setAdapter(adapter);
            if (!mListShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter. It is now time to show it.
                setListShown(true, getView().getWindowToken() != null);
            }
        }
    }
    
    public void setListShown(boolean shown, String loading) {
    	setLoadingText(loading);
        setListShown(shown, true);
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (mProgressContainer == null) {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if(null != mStandardEmptyView){
                mStandardEmptyView.setText("");
            }
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }

    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    public void setSelection(int position) {
        ensureList();
        mList.setSelection(position);
    }
}