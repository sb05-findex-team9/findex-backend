package com.codeit.findex.indexInfo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.findex.indexInfo.domain.IndexInfo;

public interface IndexInfoRepository extends JpaRepository<IndexInfo,Long> {
	@Query(
		value = """
			    select i from IndexInfo i
			    where (:indexClassification is null or i.indexClassification like concat('%', cast(:indexClassification as string), '%'))
			        and (:indexName is null or i.indexName like concat('%', cast(:indexName as string), '%'))
			        and (:favorite is null or i.favorite = :favorite)
			        and (:cursor is null or
			            (:sortDirection = 'asc' and (i.indexClassification > :cursor or (i.indexClassification = :cursor and i.id > :idAfter))) or
			            (:sortDirection = 'desc' and (i.indexClassification < :cursor or (i.indexClassification = :cursor and i.id < :idAfter))))
			    order by 
			        case when :sortDirection = 'asc' then i.indexClassification end asc,
			        case when :sortDirection = 'desc' then i.indexClassification end desc,
			        case when :sortDirection = 'asc' then i.id end asc,
			        case when :sortDirection = 'desc' then i.id end desc
			"""
	)
	Slice<IndexInfo> findAllByIndexClassificationCursor(
		@Param("indexClassification") String indexClassification,
		@Param("indexName") String indexName,
		@Param("favorite") Boolean favorite,
		@Param("idAfter") Integer idAfter,
		@Param("cursor") String cursor,
		@Param("sortDirection") String sortDirection,
		Pageable pageable
	);

	@Query(
		value = """
        select i from IndexInfo i
        where (:indexClassification is null or i.indexClassification like concat('%', cast(:indexClassification as string), '%'))
            and (:indexName is null or i.indexName like concat('%', cast(:indexName as string), '%'))
            and (:favorite is null or i.favorite = :favorite)
            and (:cursor is null or
                (:sortDirection = 'asc' and (i.indexName > :cursor or (i.indexName = :cursor and i.id > :idAfter))) or
                (:sortDirection = 'desc' and (i.indexName < :cursor or (i.indexName = :cursor and i.id < :idAfter))))
        order by 
            case when :sortDirection = 'asc' then i.indexName end asc,
            case when :sortDirection = 'desc' then i.indexName end desc,
            case when :sortDirection = 'asc' then i.id end asc,
            case when :sortDirection = 'desc' then i.id end desc
    """
	)
	Slice<IndexInfo> findAllByIndexNameCursor(
		@Param("indexClassification") String indexClassification,
		@Param("indexName") String indexName,
		@Param("favorite") Boolean favorite,
		@Param("idAfter") Integer idAfter,
		@Param("cursor") String cursor,
		@Param("sortDirection") String sortDirection,
		Pageable pageable
	);

	@Query(
		value = """
        select i from IndexInfo i
        where (:indexClassification is null or i.indexClassification like concat('%', cast(:indexClassification as string), '%'))
            and (:indexName is null or i.indexName like concat('%', cast(:indexName as string), '%'))
            and (:favorite is null or i.favorite = :favorite)
            and (:cursor is null or
                (:sortDirection = 'asc' and (i.employedItemsCount > :cursor or (i.employedItemsCount = :cursor and i.id > :idAfter))) or
                (:sortDirection = 'desc' and (i.employedItemsCount < :cursor or (i.employedItemsCount = :cursor and i.id < :idAfter))))
        order by 
            case when :sortDirection = 'asc' then i.employedItemsCount end asc,
            case when :sortDirection = 'desc' then i.employedItemsCount end desc,
            case when :sortDirection = 'asc' then i.id end asc,
            case when :sortDirection = 'desc' then i.id end desc
    """
	)
	Slice<IndexInfo> findAllByEmployedItemsCountCursor(
		@Param("indexClassification") String indexClassification,
		@Param("indexName") String indexName,
		@Param("favorite") Boolean favorite,
		@Param("idAfter") Integer idAfter,
		@Param("cursor") Integer cursor,
		@Param("sortDirection") String sortDirection,
		Pageable pageable
	);

	Optional<IndexInfo> getIndexInfoById(Long id);

	@Query("""
		select count(i) from IndexInfo i
		where (:indexClassification is null or i.indexClassification like concat('%', cast(:indexClassification as string), '%'))
		    and (:indexName is null or i.indexName like concat('%', cast(:indexName as string), '%'))
		    and (:favorite is null or i.favorite = :favorite)
		""")
	Long countByFilters(
		@Param("indexClassification") String indexClassification,
		@Param("indexName") String indexName,
		@Param("favorite") Boolean favorite
	);

	List<IndexInfo> findByIndexNameAndIndexClassification(String indexName, String indexClassification);

	List<IndexInfo> findByFavoriteTrue();
}