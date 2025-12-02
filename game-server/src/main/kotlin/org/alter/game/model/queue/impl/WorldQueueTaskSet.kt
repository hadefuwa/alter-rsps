package org.alter.game.model.queue.impl

import org.alter.game.model.queue.QueueTask
import org.alter.game.model.queue.QueueTaskSet
import kotlin.coroutines.resume

/**
 * A [QueueTaskSet] implementation for [org.alter.game.model.World].
 * All [org.alter.game.model.queue.QueueTask]s are handled every tick.
 *
 * @author Tom <rspsmods@gmail.com>
 */
class WorldQueueTaskSet : QueueTaskSet() {
    override fun cycle() {
        // Create a snapshot of the queue to avoid ConcurrentModificationException
        val snapshot = synchronized(queue) { queue.toList() }
        val tasksToRemove = mutableListOf<QueueTask>()
        
        for (task in snapshot) {
            if (!task.invoked) {
                task.invoked = true
                task.coroutine.resume(Unit)
            }

            task.cycle()

            if (!task.suspended()) {
                /*
                 * Task is no longer in a suspended state, which means its job is
                 * complete. Collect it for removal after iteration to avoid
                 * ConcurrentModificationException.
                 */
                tasksToRemove.add(task)
            }
        }
        
        // Remove completed tasks using synchronized block
        synchronized(queue) {
            tasksToRemove.forEach { task -> queue.remove(task) }
        }
    }
}
