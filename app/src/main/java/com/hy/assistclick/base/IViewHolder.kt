package com.hy.assistclick.base

/**
 * Created by andy (https://github.com/andyxialm)
 * Creation time: 17/8/31 15:12
 * Description:IViewHolder
 */
interface IViewHolder<T> {
    fun bind(item: T)
}