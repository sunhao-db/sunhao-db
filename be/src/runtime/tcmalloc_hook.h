// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

#pragma once

#include <gperftools/malloc_hook.h>
#include <gperftools/nallocx.h>
#include <gperftools/tcmalloc.h>

#include "runtime/mem_tracker.h"
#include "runtime/thread_context.h"

// Notice: modify the command in New/Delete Hook should be careful enough!,
// and should be as simple as possible, otherwise it may cause weird errors. E.g:
//  1. The first New Hook call of the process may be before some variables of
//  the process are initialized.
//  2. Allocating memory in the Hook command causes the Hook to be entered again,
//  infinite recursion.
//  3. TCMalloc hook will be triggered during the process of initializing/Destructor
//  memtracker shared_ptr, Using the object pointed to by this memtracker shared_ptr
//  in TCMalloc hook may cause crash.
//  4. Modifying additional thread local variables in ThreadContext construction and
//  destructor to control the behavior of consume can lead to unexpected behavior,
//  like this: if (LIKELY(doris::start_thread_mem_tracker)) {
void new_hook(const void* ptr, size_t size) {
    if (doris::btls_key != doris::EMPTY_BTLS_KEY && doris::bthread_tls != nullptr) {
        // Currently in bthread, consume thread context mem tracker in bthread tls.
        if (doris::btls_key != doris::bthread_tls_key) {
            // pthread switch occurs, updating bthread_tls and bthread_tls_key cached in pthread tls.
            doris::bthread_tls =
                    static_cast<doris::ThreadContext*>(bthread_getspecific(doris::btls_key));
            doris::bthread_tls_key = doris::btls_key;
        }
        doris::bthread_tls->_thread_mem_tracker_mgr->cache_consume(tc_nallocx(size, 0));
    } else if (doris::thread_local_ctx._init) {
        doris::thread_local_ctx._tls->_thread_mem_tracker_mgr->cache_consume(tc_nallocx(size, 0));
    }
}

void delete_hook(const void* ptr) {
    if (doris::btls_key != doris::EMPTY_BTLS_KEY && doris::bthread_tls != nullptr) {
        if (doris::btls_key != doris::bthread_tls_key) {
            doris::bthread_tls =
                    static_cast<doris::ThreadContext*>(bthread_getspecific(doris::btls_key));
            doris::bthread_tls_key = doris::btls_key;
        }
        doris::bthread_tls->_thread_mem_tracker_mgr->cache_consume(
                -tc_malloc_size(const_cast<void*>(ptr)));
    } else if (doris::thread_local_ctx._init) {
        doris::thread_local_ctx._tls->_thread_mem_tracker_mgr->cache_consume(
                -tc_malloc_size(const_cast<void*>(ptr)));
    }
}

void init_hook() {
    MallocHook::AddNewHook(&new_hook);
    MallocHook::AddDeleteHook(&delete_hook);
}

// For later debug.
// static void destroy_hook() {
//     MallocHook::RemoveNewHook(&new_hook);
//     MallocHook::RemoveDeleteHook(&delete_hook);
// }
