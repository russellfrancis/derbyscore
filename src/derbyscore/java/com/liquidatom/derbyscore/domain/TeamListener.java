package com.liquidatom.derbyscore.domain;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public interface TeamListener {

    /**
     * Invoked when the state of the team is changed.
     *
     * @param team The team which recently changed.
     */
    public void onChanged(Team team);
}
