CREATE TABLE IF NOT EXISTS public.items_rows
(
    store_id bigint NOT NULL,
    shelving_group_id bigint NOT NULL,
    shelving_id bigint NOT NULL,
    shelf_id bigint NOT NULL,
    items_row_id bigint NOT NULL,
    catalog_item bigint NOT NULL,
    "count" int NOT NULL,
    CONSTRAINT items_rows_pkey PRIMARY KEY (store_id, shelving_group_id, shelving_id, shelf_id, items_row_id)
);

INSERT INTO public.items_rows(store_id, shelving_group_id, shelving_id, shelf_id, items_row_id, catalog_item, count)
VALUES (1, 2, 3, 4, 5, 6, 7);