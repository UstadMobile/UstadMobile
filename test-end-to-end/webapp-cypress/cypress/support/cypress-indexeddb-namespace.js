
  declare namespace Cypress {
    interface Chainable<Subject> {
      clearIndexedDb(databaseName: string): void;
      openIndexedDb(databaseName: string, version?: number): Chainable<IDBDatabase>;
      createObjectStore(storeName: string): Chainable<IDBObjectStore>;
      getStore(storeName: string): Chainable<IDBObjectStore>;
      createItem(key: string, value: unknown): Chainable<IDBObjectStore>;
      readItem<T = unknown>(key: IDBValidKey | IDBKeyRange): Chainable<T>;
      updateItem(key: string, value: unknown): Chainable<IDBObjectStore>;
      deleteItem(key: string): Chainable<IDBObjectStore>;
    }
  }
