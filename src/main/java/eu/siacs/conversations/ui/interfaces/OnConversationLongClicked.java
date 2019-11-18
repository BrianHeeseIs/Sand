package eu.siacs.conversations.ui.interfaces;

import eu.siacs.conversations.entities.Conversation;
import eu.siacs.conversations.ui.adapter.ConversationAdapter;

public interface OnConversationLongClicked {
	void onConversationLongClicked(ConversationAdapter.ConversationViewHolder viewHolder, Conversation conversation);
}
