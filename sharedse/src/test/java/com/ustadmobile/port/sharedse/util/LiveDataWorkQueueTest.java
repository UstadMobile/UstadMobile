package com.ustadmobile.port.sharedse.util;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class LiveDataWorkQueueTest {

    private class TestWorkItem {

        private long uid;

        private Runnable mockRunnable;

        private TestWorkItem(long uid, Runnable mockRunnable) {
            this.uid = uid;
            this.mockRunnable = mockRunnable;
        }

        public long getUid() {
            return uid;
        }

        public Runnable getMockRunnable() {
            return mockRunnable;
        }
    }

    private class TestWorkItemHolder implements LiveDataWorkQueue.WorkQueueItemHolder<TestWorkItem>{

        private TestWorkItem item;

        private Runnable runnable;

        private TestWorkItemHolder(TestWorkItem item) {
            this.item = item;
        }

        @Override
        public long getUid() {
            return item.getUid();
        }

        @Override
        public Runnable makeRunnable() {
            if(this.runnable == null) {
                runnable = item.getMockRunnable();
                return runnable;
            }

            throw new IllegalStateException("makeRunnable should be called only once");
        }
    }


    @Test
    public void givenEmptyQueue_whenStarted_willRunItems() {
        LiveDataWorkQueue<TestWorkItem> liveDataWorkQueue = new LiveDataWorkQueue<>(4);
        final List<TestWorkItem> pendingItems = new Vector<>();
        final List<Runnable> mockRunnables = new Vector<>();

        int numWorkItems = 100;

        @SuppressWarnings("unchecked")
        UmLiveData<List<TestWorkItem>> dataSource = (UmLiveData<List<TestWorkItem>>)Mockito.mock(UmLiveData.class);

        AtomicReference<UmObserver<List<TestWorkItem>>> observerRef = new AtomicReference<>();

        doAnswer(invocation -> {
            observerRef.set(invocation.getArgument(0));
            observerRef.get().onChanged(new Vector<>(pendingItems));
            return null;
        }).when(dataSource).observeForever(any());

        for(int i = 0; i < numWorkItems; i++) {
            Runnable mockRunnable = Mockito.mock(Runnable.class);
            TestWorkItem mockItem = new TestWorkItem(i, mockRunnable);
            doAnswer((invocation -> {
                synchronized (pendingItems) {
                    pendingItems.remove(mockItem);
                    observerRef.get().onChanged(new Vector<>(pendingItems));
                }

                return null;
            })).when(mockRunnable).run();
            pendingItems.add(mockItem);
            mockRunnables.add(mockRunnable);
        }

        liveDataWorkQueue.setAdapter((item -> new TestWorkItemHolder(item)));
        liveDataWorkQueue.start(dataSource);

        for(int i = 0; i < mockRunnables.size(); i++) {
            verify(mockRunnables.get(i), timeout(10000)).run();
        }
    }

}
