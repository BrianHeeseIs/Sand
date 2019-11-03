/*
 * Copyright (c) 2018, Daniel Gultsch All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package eu.siacs.conversations.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.FragmentConversationsOverviewBinding;
import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.ui.adapter.ConversationAdapter;
import eu.siacs.conversations.ui.interfaces.OnConversationSelected;
import eu.siacs.conversations.ui.util.MenuDoubleTabUtil;
import eu.siacs.conversations.ui.util.PendingActionHelper;
import eu.siacs.conversations.ui.util.PendingItem;
import eu.siacs.conversations.ui.util.ScrollState;

public class WipeConversationsOverviewFragment extends XmppFragment {

	private static final String STATE_SCROLL_POSITION = WipeConversationsOverviewFragment.class.getName()+".scroll_state";

	private final List<Conversation> conversations = new ArrayList<>();
	private final PendingItem<ScrollState> pendingScrollState = new PendingItem<>();
	private FragmentConversationsOverviewBinding binding;
	private ConversationAdapter conversationsAdapter;
	private XmppActivity activity;
	private PendingActionHelper pendingActionHelper = new PendingActionHelper();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			return;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof XmppActivity) {
			this.activity = (XmppActivity) activity;
		} else {
			throw new IllegalStateException("Trying to attach fragment to activity that is not an XmppActivity");
		}
	}

	@Override
	public void onPause() {
		Log.d(Config.LOGTAG,"WipeConversationsOverviewFragment.onPause()");
		pendingActionHelper.execute();
		super.onPause();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.activity = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wipe_conversations_overview, container, false);
		this.conversationsAdapter = new ConversationAdapter(this.activity, this.conversations);
		this.conversationsAdapter.setConversationClickListener((view, conversation) -> {
			if (activity instanceof OnConversationSelected) {
				((OnConversationSelected) activity).onConversationSelected(conversation);
			} else {
				Log.w(WipeConversationsOverviewFragment.class.getCanonicalName(), "Activity does not implement OnConversationSelected");
			}
		});

		this.binding.list.setAdapter(this.conversationsAdapter);
		this.binding.list.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
		return binding.getRoot();
	}

	@Override
	public void onBackendConnected() {
		refresh();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		ScrollState scrollState = getScrollState();
		if (scrollState != null) {
			bundle.putParcelable(STATE_SCROLL_POSITION, scrollState);
		}
	}

	private ScrollState getScrollState() {
		if (this.binding == null) {
			return null;
		}
		LinearLayoutManager layoutManager = (LinearLayoutManager) this.binding.list.getLayoutManager();
		int position = layoutManager.findFirstVisibleItemPosition();
		final View view = this.binding.list.getChildAt(0);
		if (view != null) {
			return new ScrollState(position,view.getTop());
		} else {
			return new ScrollState(position, 0);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(Config.LOGTAG, "WipeConversationsOverviewFragment.onStart()");
		if (activity.xmppConnectionService != null) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(Config.LOGTAG, "WipeConversationsOverviewFragment.onResume()");
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (MenuDoubleTabUtil.shouldIgnoreTap()) {
			return false;
		}
		switch (item.getItemId()) {
			case R.id.action_search:
				startActivity(new Intent(getActivity(), SearchActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	void refresh() {
		if (this.binding == null || this.activity == null) {
			Log.d(Config.LOGTAG,"WipeConversationsOverviewFragment.refresh() skipped updated because view binding or activity was null");
			return;
		}
		this.activity.xmppConnectionService.populateWithOrderedConversations(this.conversations);
//		Conversation removed = this.swipedConversation.peek();
//		if (removed != null) {
//			if (removed.isRead()) {
//				this.conversations.remove(removed);
//			} else {
//				pendingActionHelper.execute();
//			}
//		}
		this.conversationsAdapter.notifyDataSetChanged();
		ScrollState scrollState = pendingScrollState.pop();
		if (scrollState != null) {
			setScrollPosition(scrollState);
		}
	}

	private void setScrollPosition(ScrollState scrollPosition) {
		if (scrollPosition != null) {
			LinearLayoutManager layoutManager = (LinearLayoutManager) binding.list.getLayoutManager();
			layoutManager.scrollToPositionWithOffset(scrollPosition.position, scrollPosition.offset);
		}
	}
}
