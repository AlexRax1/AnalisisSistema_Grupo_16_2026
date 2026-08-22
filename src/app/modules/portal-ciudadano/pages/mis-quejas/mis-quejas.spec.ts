import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MisQuejas } from './mis-quejas';

describe('MisQuejas', () => {
  let component: MisQuejas;
  let fixture: ComponentFixture<MisQuejas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisQuejas],
    }).compileComponents();

    fixture = TestBed.createComponent(MisQuejas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
