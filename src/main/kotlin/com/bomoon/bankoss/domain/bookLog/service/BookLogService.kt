package com.bomoon.bankoss.domain.bookLog.service

import com.bomoon.bankoss.domain.book.dto.BookDto
import com.bomoon.bankoss.domain.book.model.Book
import com.bomoon.bankoss.domain.book.repository.BookRepository
import com.bomoon.bankoss.domain.book.service.BookService
import com.bomoon.bankoss.domain.bookLog.dto.BookLogDto
import com.bomoon.bankoss.domain.bookLog.exception.AlreadyDeletedLogException
import com.bomoon.bankoss.domain.bookLog.exception.CannotFindBookLogException
import com.bomoon.bankoss.domain.bookLog.exception.DidNotDeleteLogException
import com.bomoon.bankoss.domain.bookLog.model.BookLog
import com.bomoon.bankoss.domain.bookLog.repository.BookLogRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import kotlin.math.abs

@Service
class BookLogService(
    private val bookLogRepository: BookLogRepository,
    private val bookRepository: BookRepository,
    private val bookService: BookService,
) {
    fun addLog(addLogRequest: BookLogDto.AddLogRequest, book: Book): BookDto.BookResponse {
        val category = addLogRequest.category
        val moneyType = addLogRequest.moneyType
        val money = addLogRequest.money
        val memo = addLogRequest.memo

        val newLog = bookLogRepository.save(
            BookLog(
                category = category, moneyType = moneyType, money = money, memo = memo
            )
        )
        newLog.book = book
        book.balance = bookService.calculateBalance(book.balance, moneyType, money)
        bookRepository.save(book)

        return BookDto.BookResponse(book)
    }

    fun editLog(modifyLogRequest: BookLogDto.ModifyLogRequest, log: BookLog, book: Book): BookDto.BookResponse {
        var type: Boolean = log.moneyType
        var moneyForBal: Int = log.money
        if (modifyLogRequest.category != null) log.category = modifyLogRequest.category
        if (modifyLogRequest.moneyType != null) {
            type = !(modifyLogRequest.moneyType xor log.moneyType)
            log.moneyType = modifyLogRequest.moneyType
        }
        if (modifyLogRequest.money != null) {
            if (type) moneyForBal += modifyLogRequest.money
            else moneyForBal -= modifyLogRequest.money
            log.money = modifyLogRequest.money
        }
        if (modifyLogRequest.memo != null) log.memo = modifyLogRequest.memo

        val newBalance = bookService.calculateBalance(book.balance, type, abs(moneyForBal))
        book.balance = newBalance
        bookLogRepository.save(log)

        return BookDto.BookResponse(book)
    }

    fun deleteLog(log: BookLog, book: Book): BookDto.BookResponse {
        if (!log.isDeleted) throw AlreadyDeletedLogException()
        log.isDeleted = false

        val newBalance = bookService.calculateBalance(book.balance, !log.moneyType, log.money)
        book.balance = newBalance
        bookLogRepository.save(log)

        return BookDto.BookResponse(book)
    }

    fun restoreLog(log: BookLog, book: Book): BookDto.BookResponse {
        if (log.isDeleted) throw DidNotDeleteLogException()
        log.isDeleted = true

        val newBalance = bookService.calculateBalance(book.balance, log.moneyType, log.money)
        book.balance = newBalance
        bookLogRepository.save(log)

        return BookDto.BookResponse(book)
    }

    fun getThisLog(id: Long): BookLog {
        return bookLogRepository.findByIdOrNull(id) ?: throw CannotFindBookLogException()
    }
}
