package com.liquidatom.derbyscore.domain;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: TeamListener.java 4 2010-03-12 11:40:07Z russ $
 */
public interface TeamListener {

    /**
     * Invoked when the state of the team is changed.
     *
     * @param team The team which recently changed.
     */
    public void onChanged(Team team);
}
