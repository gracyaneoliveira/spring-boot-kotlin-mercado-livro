package com.mercadolivro.service

import com.mercadolivro.enums.BookStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.exception.BadRequestException
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.BookModel
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.BookRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository
) {

    fun create(book: BookModel) {
        bookRepository.save(book)
    }

    fun findAll(pageable: Pageable): Page<BookModel> {
        return bookRepository.findAll(pageable)
    }

    fun findActives(pageable: Pageable): Page<BookModel> {
        return bookRepository.findByStatus(BookStatus.ATIVO, pageable)
    }

    fun findById(id: Int): BookModel {
        return bookRepository.findById(id)
            .orElseThrow { NotFoundException(Errors.ML101.message.format(id), Errors.ML101.code) }
    }

    fun delete(id: Int) {
        val book = findById(id)
        book.status = BookStatus.CANCELADO
        update(book)
    }

    fun update(book: BookModel) {
        bookRepository.save(book)
    }

    fun deleteByCustomer(customer: CustomerModel) {
        val books = bookRepository.findByCustomer(customer)
        for (book in books) {
            book.status = BookStatus.DELETADO
        }
        bookRepository.saveAll(books)
    }

    fun findAlldById(booksId: Set<Int>): List<BookModel> {
        return bookRepository.findAllById(booksId).toList()
    }

    fun purchase(books: MutableList<BookModel>) {
        for (book in books) {
            book.status = BookStatus.VENDIDO
        }
        bookRepository.saveAll(books)
    }

    fun checkIfValidStatusBook(books: MutableList<BookModel>) {
        val booksDB = bookRepository.findAllById(books.map { it.id })

        val element = booksDB.firstOrNull { it.status == BookStatus.DELETADO || it.status == BookStatus.VENDIDO || it.status == BookStatus.CANCELADO }
        element?.let {
            throw BadRequestException(Errors.ML103.message.format(it.status), Errors.ML103.code)
        }
    }
}
