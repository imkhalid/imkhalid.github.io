package com.khalid.vyntra.data.repository

import com.khalid.vyntra.data.local.dao.CategoryDao
import com.khalid.vyntra.data.mapper.toDomain
import com.khalid.vyntra.data.mapper.toEntity
import com.khalid.vyntra.domain.model.Category
import com.khalid.vyntra.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> {
        return categoryDao.getCategoryWithProductCount().map { list ->
            list.map { row ->
                Category(
                    id = row.id,
                    name = row.name,
                    description = row.description.ifEmpty { null },
                    productCount = row.productCount,
                    createdAt = row.createdAt
                )
            }
        }
    }

    override suspend fun getById(id: Long): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    override suspend fun add(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    override suspend fun update(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun delete(id: Long) {
        val entity = categoryDao.getById(id) ?: return
        categoryDao.delete(entity)
    }
}
