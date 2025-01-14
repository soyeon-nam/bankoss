package com.bomoon.bankoss.domain.book.service

import com.bomoon.bankoss.domain.book.dto.BookDto
import com.bomoon.bankoss.domain.book.exception.CannotFindBookException
import com.bomoon.bankoss.domain.book.model.Book
import com.bomoon.bankoss.domain.book.repository.BookRepository
import com.bomoon.bankoss.domain.user.model.User
import com.bomoon.bankoss.domain.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
) {
    fun createBook(user: User, createRequest: BookDto.CreateRequest): BookDto.BookResponse {
        val title = createRequest.title
        val type = createRequest.type
        val memo = createRequest.memo

        val newBook = bookRepository.save(Book(title = title, type = type, memo = memo))
        newBook.user = user
        userRepository.save(user)
        return BookDto.BookResponse(newBook)
    }

    fun calculateBalance(bookBalance: Int, moneyType: Boolean, money: Int): Int {
        val newBalance: Int
        if (moneyType) newBalance = bookBalance + money
        else newBalance = bookBalance - money
        return newBalance
    }

    fun getThisBook(id: Long): Book {
        return bookRepository.findByIdOrNull(id) ?: throw CannotFindBookException()
    }
}
