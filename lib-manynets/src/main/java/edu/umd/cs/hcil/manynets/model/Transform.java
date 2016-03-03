/*
 *  This file is part of ManyNets.
 *
 *  ManyNets is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  ManyNets is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with ManyNets.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  ManyNets was created at the Human Computer Interaction Lab, 
 *  University of Maryland at College Park. See the README file for details
 */

package edu.umd.cs.hcil.manynets.model;

import java.util.Set;

/**
 * Contains a transformation that affects TableWrapper. The resulting
 * TW can then be represented, or substitute the original.
 *
 * Examples of graph-level transforms include
 * - retain only main component / remove main component
 * - build ego networks
 * - slice according to time
 * - keep all currently-selected graphs
 * - calculate new attributes
 *
 * Transformations can be configured; their configuration objects can be
 * edited through a GUI or set from a file, and persisted for aggregation into
 * larger transformations and history-preservation purposes.
 *
 * @author Manuel Freire
 */
public interface Transform {

    /**
     * Initialize the transform; will be called before calling 
     * and apply.
     *
     * @param tw tablewrapper
     * @param options used to configure this transform
     * @param results from a previous run with identical population and options
     *  (use null if no results available)
     */
    public void init(TableWrapper tw, TransformOptions options,
                TransformResults results);

    /**
     * Apply the transform on the given member.
     * Should be thread-safe in the event of multiple simultaneous calls.
     * @param id of member
     * @param pm a progress monitor
     */
    public void apply(int id, ProgressMonitor pm);

    /**
     * Restricts the transform, when applied, to the specified set of IDs
     * @param ids
     */
    public void restrictTo(Set<Integer> ids);

    /**
     * checks whether this ID should be processed or not (ie.: if there is a
     * restrictTo setting, and if it falls within that whitelist)
     * @param ids
     */
    public boolean shouldProcess(int id);

    /**
     * Only during 'apply':
     * Cancel a running application
     */
    public void cancel();

    /**
     * Get the results. Should only be called after init
     * and all applications have been performed. The transform /may/ reply with
     * the same TW that it was initialized with.
     */
    public TableWrapper getResultWrapper();

    /**
     * Get the options used to configure this transform. Only valid after init.
     */
    public TransformOptions getOptions();

    /**
     * Get the results of this transform, if any.
     */
    public TransformResults getResults();


    /**
     * An interface that allows the status of the transform to be monitored
     * or reported
     */
    interface ProgressMonitor {
        /**
         * Increment progress, with an optional message.
         * @param id
         * @param message
         * @param progress: from 0 to 1
         */
        public void setProgress(int id, String message, double progress);
    }
}
