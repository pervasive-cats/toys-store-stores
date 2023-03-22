CREATE TABLE IF NOT EXISTS public.stores
(
    id bigserial NOT NULL,
    shelving_group_id bigint NOT NULL,
    shelving_id bigint NOT NULL,
    shelf_id bigint NOT NULL,
    items_row_id bigint NOT NULL,
    catalog_item bigint NOT NULL,
    item_id bigint NOT NULL,
    CONSTRAINT stores_pkey PRIMARY KEY (id)
);